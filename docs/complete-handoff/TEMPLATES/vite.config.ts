// Proposed Vite dev proxy only. Requires actual React/Vite package setup.
import {defineConfig} from "vite";
import react from "@vitejs/plugin-react";
export default defineConfig({
 plugins:[react()],
 server:{
   host:"127.0.0.1",
   port:5173,
   proxy:{"/api":{target:"http://127.0.0.1:8080",changeOrigin:true}}
 }
});
