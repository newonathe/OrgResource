package edu.orgresource.extended.service;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToLongFunction;

import org.springframework.data.domain.PageRequest;

import edu.orgresource.extended.api.ApiDtos;
import edu.orgresource.extended.api.C1ApiException;
import edu.orgresource.extended.identity.C1Secrets;

record WorkspacePageWindow(int limit, long after, byte[] scope) {
    private static final int SCOPE_BYTES = 32;
    private static final int TOKEN_BYTES = SCOPE_BYTES + Long.BYTES;
    private static final int ENCODED_LENGTH = 54;

    static WorkspacePageWindow of(int limit, String cursor, long organizationId, String list, String filters) {
        if (limit < 1 || limit > 100)
            throw new C1ApiException(400, "INVALID_PAGE_SIZE", "limit must be between 1 and 100.");
        byte[] scope = C1Secrets.sha256("workspace-v1|" + organizationId + "|" + list + "|" + filters);
        if (cursor == null) return new WorkspacePageWindow(limit, 0, scope);
        if (cursor.length() != ENCODED_LENGTH) throw invalidCursor();
        byte[] token;
        try {
            token = Base64.getUrlDecoder().decode(cursor);
        } catch (IllegalArgumentException malformed) {
            throw invalidCursor();
        }
        if (token.length != TOKEN_BYTES || !Base64.getUrlEncoder().withoutPadding().encodeToString(token).equals(cursor))
            throw invalidCursor();
        if (!MessageDigest.isEqual(Arrays.copyOf(token, SCOPE_BYTES), scope))
            throw new C1ApiException(404, "NOT_FOUND", "The cursor is not available for this workspace query.");
        long after = ByteBuffer.wrap(token, SCOPE_BYTES, Long.BYTES).getLong();
        if (after <= 0) throw invalidCursor();
        return new WorkspacePageWindow(limit, after, scope);
    }

    private static C1ApiException invalidCursor() {
        return new C1ApiException(400, "INVALID_CURSOR", "The page cursor is invalid.");
    }

    PageRequest queryLimit() { return PageRequest.of(0, limit + 1); }

    <T> List<T> visible(List<T> rows) {
        return rows.subList(0, Math.min(limit, rows.size()));
    }

    <T, R> ApiDtos.WorkspacePage<R> result(List<T> rows, ToLongFunction<T> id, Function<T, R> view) {
        String next = null;
        if (rows.size() > limit) {
            long last = id.applyAsLong(rows.get(limit - 1));
            byte[] token = ByteBuffer.allocate(TOKEN_BYTES).put(scope).putLong(last).array();
            next = Base64.getUrlEncoder().withoutPadding().encodeToString(token);
        }
        return new ApiDtos.WorkspacePage<>(visible(rows).stream().map(view).toList(), next);
    }
}
