// Candidate code only. Test with real Jersey API before relying on it.
export class ApiError extends Error {
  constructor(public status:number, public code:string, message:string) {
    super(message); this.name="ApiError";
  }
}
async function request<T>(path:string, method:"GET"|"POST", body?:unknown, signal?:AbortSignal):Promise<T> {
  const res=await fetch("/api"+path, {
    method, signal,
    headers: body===undefined ? {Accept:"application/json"} : {Accept:"application/json","Content-Type":"application/json"},
    body: body===undefined ? undefined : JSON.stringify(body)
  });
  const text=await res.text(); let parsed:unknown=null;
  if (text.length) {
    try { parsed=JSON.parse(text); }
    catch { throw new ApiError(res.status,"INVALID_SERVER_RESPONSE","Server returned invalid JSON."); }
  }
  if (!res.ok) {
    const err = parsed && typeof parsed==="object" ? parsed as Partial<{code:string,message:string}> : null;
    throw new ApiError(res.status,err?.code??"REQUEST_FAILED",err?.message??"Request failed.");
  }
  return parsed as T;
}
export function apiGet<T>(path:string,signal?:AbortSignal){return request<T>(path,"GET",undefined,signal);}
export function apiPost<T>(path:string,body?:unknown){return request<T>(path,"POST",body);}
