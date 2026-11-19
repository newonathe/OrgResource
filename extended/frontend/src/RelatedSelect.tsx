import { useEffect, useState } from "react";
import type { WorkspacePage } from "./api/client";
import { explainError } from "./domain";
import { Button, PageNavigation } from "./ui";

interface RelatedSelectProps<T extends { id: number }> {
  id: string;
  label: string;
  selection: T | null;
  onChange: (item: T | null) => void;
  load: (cursor: string | null, search: string, signal: AbortSignal) => Promise<WorkspacePage<T>>;
  optionLabel: (item: T) => string;
  allLabel?: string;
  reload?: number;
}

export function RelatedSelect<T extends { id: number }>({
  id, label, selection, onChange, load, optionLabel, allLabel, reload = 0,
}: RelatedSelectProps<T>) {
  const [search, setSearch] = useState("");
  const [cursors, setCursors] = useState<(string | null)[]>([null]);
  const [result, setResult] = useState<WorkspacePage<T> | null>(null);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);
  const cursor = cursors.at(-1) ?? null;

  useEffect(() => {
    const controller = new AbortController();
    const timer = window.setTimeout(() => {
      void load(cursor, search, controller.signal).then(page => {
        if (!controller.signal.aborted) { setResult(page); setError(""); }
      }).catch((failure: unknown) => {
        if (!controller.signal.aborted) setError(explainError(failure));
      });
    }, search ? 150 : 0);
    return () => { window.clearTimeout(timer); controller.abort(); };
  }, [cursor, load, search, retry, reload]);

  useEffect(() => {
    if (allLabel || !result) return;
    const current = selection ? result.items.find(row => row.id === selection.id) : result.items[0];
    if (current && current !== selection) onChange(current);
  }, [allLabel, onChange, result, selection]);

  const rows = result?.items ?? [];
  const options = selection && !rows.some(row => row.id === selection.id) ? [selection, ...rows] : rows;
  const loading = result === null && !error;
  function goToPage(next: string | null) {
    setResult(null); setError("");
    setCursors(previous => next ? [...previous, next] : previous.slice(0, -1));
  }

  return <div className="related-select">
    <label htmlFor={id}>{label}</label>
    <input aria-label={"Find " + label.toLowerCase()} type="search" maxLength={100} value={search} placeholder="Search choices"
      onChange={event => { setResult(null); setError(""); setCursors([null]); setSearch(event.target.value); }} />
    <select id={id} value={selection?.id ?? ""} required={!allLabel}
      onChange={event => onChange(options.find(row => String(row.id) === event.target.value) ?? null)}>
      <option value="" disabled={!allLabel}>{allLabel ?? (loading ? "Loading choices…" : "Choose a record")}</option>
      {options.map(row => <option key={row.id} value={row.id}>{optionLabel(row)}</option>)}
    </select>
    {error && <div role="alert"><small>{error}</small> <Button type="button" onClick={() => {
      setResult(null); setError(""); setRetry(count => count + 1);
    }}>Retry choices</Button></div>}
    {(loading || error || cursors.length > 1 || result?.nextCursor || rows.length === 0) &&
      <PageNavigation label={label + " choices"} page={cursors.length} count={rows.length}
        hasNext={!!result?.nextCursor && !error} loading={loading} unavailable={!!error}
        onPrevious={() => goToPage(null)} onNext={() => { if (result?.nextCursor) goToPage(result.nextCursor); }} />}
  </div>;
}
