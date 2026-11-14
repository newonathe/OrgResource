import { afterEach, describe, expect, it, vi } from "vitest";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { App } from "./App";

interface JsonResponse {
  ok: boolean;
  status: number;
  text: () => Promise<string>;
}

function jsonResponse(body: unknown, status = 200): Response {
  const response: JsonResponse = {
    ok: status >= 200 && status < 300,
    status,
    text: async () => JSON.stringify(body),
  };
  return response as Response;
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe("OrgResource screens", () => {
  it("creates and lists a Department using the real API contract", async () => {
    const departments: Array<{ id: number; name: string }> = [];
    const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input);
      const method = init?.method ?? "GET";
      if (url === "/api/departments" && method === "GET") return jsonResponse(departments);
      if (url === "/api/departments" && method === "POST") {
        const body = JSON.parse(String(init?.body)) as { name: string };
        const department = { id: departments.length + 1, name: body.name.trim() };
        departments.push(department);
        return jsonResponse(department, 201);
      }
      return jsonResponse({ code: "NOT_FOUND", message: "No matching test route." }, 404);
    });
    vi.stubGlobal("fetch", fetchMock);

    render(<App />);
    await screen.findByText("No departments yet");
    fireEvent.change(screen.getByLabelText("Department name"), { target: { value: "Field Operations" } });
    fireEvent.click(screen.getByRole("button", { name: "Add department" }));

    expect(await screen.findByRole("button", { name: "Edit Field Operations" })).toBeInTheDocument();
    expect(screen.getByRole("status")).toHaveTextContent("Department saved.");
    expect(fetchMock).toHaveBeenCalledWith("/api/departments", expect.objectContaining({ method: "POST" }));
  });

  it("requires review after stale Resource quantity and preserves the user draft", async () => {
    let serverResource = { id: 41, name: "Projector", type: "REUSABLE", totalQuantity: 10, availableQuantity: 8 };
    let conflictOnce = true;
    const updateBodies: Array<Record<string, unknown>> = [];
    const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input);
      const method = init?.method ?? "GET";
      if (url === "/api/departments") return jsonResponse([]);
      if (url === "/api/resources" && method === "GET") return jsonResponse([serverResource]);
      if (url === "/api/resources" && method === "POST") return jsonResponse(serverResource, 201);
      if (url === "/api/resources/41/update" && method === "POST") {
        const body = JSON.parse(String(init?.body)) as Record<string, unknown>;
        updateBodies.push(body);
        if (conflictOnce) {
          conflictOnce = false;
          serverResource = { ...serverResource, totalQuantity: 12, availableQuantity: 9 };
          return jsonResponse({ code: "STALE_RESOURCE_QUANTITY", message: "Reload before updating." }, 409);
        }
        serverResource = { ...serverResource, name: String(body.name), totalQuantity: Number(body.totalQuantity) };
        return jsonResponse(serverResource);
      }
      return jsonResponse({ code: "NOT_FOUND", message: "No matching test route." }, 404);
    });
    vi.stubGlobal("fetch", fetchMock);

    const { container } = render(<App />);
    fireEvent.click(screen.getByRole("link", { name: "Resources" }));
    const row = await screen.findByRole("row", { name: /Projector/ });
    fireEvent.click(within(row).getByRole("button", { name: "Edit Projector" }));
    fireEvent.change(screen.getByLabelText("Name"), { target: { value: "Projector for hall" } });
    fireEvent.change(screen.getByLabelText("Total quantity"), { target: { value: "8" } });
    fireEvent.click(screen.getByRole("button", { name: "Save changes" }));

    expect(await screen.findByText(/server quantity changed/i)).toBeInTheDocument();
    await waitFor(() => expect(container.querySelector(".review-guard")).toHaveTextContent("Current server total: 12"));
    expect(screen.getByLabelText("Name")).toHaveValue("Projector for hall");
    expect(screen.getByLabelText("Total quantity")).toHaveValue(8);
    expect(screen.getByRole("button", { name: "Save changes" })).toBeDisabled();
    fireEvent.click(screen.getByRole("button", { name: "I reviewed these current values" }));
    fireEvent.click(screen.getByRole("button", { name: "Save changes" }));

    await waitFor(() => expect(updateBodies).toHaveLength(2));
    expect(updateBodies[0]).toEqual({ name: "Projector for hall", totalQuantity: 8, expectedTotalQuantity: 10 });
    expect(updateBodies[1]).toEqual({ name: "Projector for hall", totalQuantity: 8, expectedTotalQuantity: 12 });
    expect(await screen.findByText("Resource updated.")).toBeInTheDocument();
  });
});
