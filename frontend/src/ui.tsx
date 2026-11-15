import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  type ButtonHTMLAttributes,
  type CSSProperties,
  type ReactNode,
} from "react";

type Theme = "light" | "dark";
interface Settings {
  theme: Theme;
  setTheme: (theme: Theme) => void;
  motionEnabled: boolean;
  setMotionEnabled: (enabled: boolean) => void;
}

const SettingsContext = createContext<Settings | null>(null);

function preferredMotion(): boolean {
  return typeof window === "undefined" || !window.matchMedia("(prefers-reduced-motion: reduce)").matches;
}

export function SettingsProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<Theme>(() => {
    const saved = window.localStorage.getItem("orgresource-theme");
    return saved === "dark" ? "dark" : "light";
  });
  const [motionEnabled, setMotionState] = useState(() => {
    const saved = window.localStorage.getItem("orgresource-motion");
    return saved === null ? preferredMotion() : saved === "on";
  });

  const setTheme = useCallback((value: Theme) => {
    setThemeState(value);
    window.localStorage.setItem("orgresource-theme", value);
  }, []);
  const setMotionEnabled = useCallback((value: boolean) => {
    setMotionState(value);
    window.localStorage.setItem("orgresource-motion", value ? "on" : "off");
  }, []);

  return (
    <SettingsContext.Provider value={{ theme, setTheme, motionEnabled, setMotionEnabled }}>
      {children}
    </SettingsContext.Provider>
  );
}

export function useSettings(): Settings {
  const settings = useContext(SettingsContext);
  if (!settings) throw new Error("SettingsProvider is required.");
  return settings;
}

export function GlassPanel({
  children,
  className = "",
  tilt = false,
  as: Tag = "section",
}: {
  children: ReactNode;
  className?: string;
  tilt?: boolean;
  as?: "section" | "article" | "div";
}) {
  const { motionEnabled } = useSettings();
  const frame = useRef<number | null>(null);
  const move = useCallback((event: React.PointerEvent<HTMLElement>) => {
    if (!motionEnabled || !window.matchMedia("(pointer: fine)").matches) return;
    const node = event.currentTarget;
    const rect = node.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    if (frame.current !== null) cancelAnimationFrame(frame.current);
    frame.current = requestAnimationFrame(() => {
      node.style.setProperty("--pointer-x", `${x}px`);
      node.style.setProperty("--pointer-y", `${y}px`);
      if (tilt) {
        const rotateY = ((x / rect.width) - 0.5) * 4;
        const rotateX = (0.5 - (y / rect.height)) * 4;
        node.style.setProperty("--tilt-x", `${rotateX}deg`);
        node.style.setProperty("--tilt-y", `${rotateY}deg`);
      }
    });
  }, [motionEnabled, tilt]);
  const leave = useCallback((event: React.PointerEvent<HTMLElement>) => {
    if (frame.current !== null) cancelAnimationFrame(frame.current);
    frame.current = null;
    event.currentTarget.style.setProperty("--tilt-x", "0deg");
    event.currentTarget.style.setProperty("--tilt-y", "0deg");
  }, []);
  useEffect(() => () => {
    if (frame.current !== null) cancelAnimationFrame(frame.current);
  }, []);

  return <Tag className={`glass-panel ${tilt ? "glass-panel-tilt" : ""} ${className}`.trim()} onPointerMove={move} onPointerLeave={leave}>{children}</Tag>;
}

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "quiet" | "danger";
  magnetic?: boolean;
};

export function Button({ variant = "secondary", magnetic = false, className = "", onPointerMove, onPointerLeave, style, ...props }: ButtonProps) {
  const { motionEnabled } = useSettings();
  const frame = useRef<number | null>(null);
  const handleMove = (event: React.PointerEvent<HTMLButtonElement>) => {
    onPointerMove?.(event);
    if (!magnetic || !motionEnabled || !window.matchMedia("(pointer: fine)").matches) return;
    const node = event.currentTarget;
    const rect = node.getBoundingClientRect();
    const x = ((event.clientX - rect.left) / rect.width - 0.5) * 6;
    const y = ((event.clientY - rect.top) / rect.height - 0.5) * 6;
    if (frame.current !== null) cancelAnimationFrame(frame.current);
    frame.current = requestAnimationFrame(() => {
      node.style.setProperty("--mag-x", `${x}px`);
      node.style.setProperty("--mag-y", `${y}px`);
    });
  };
  const handleLeave = (event: React.PointerEvent<HTMLButtonElement>) => {
    onPointerLeave?.(event);
    const node = event.currentTarget;
    if (frame.current !== null) cancelAnimationFrame(frame.current);
    frame.current = requestAnimationFrame(() => {
      node.style.setProperty("--mag-x", "0px");
      node.style.setProperty("--mag-y", "0px");
    });
  };
  useEffect(() => () => {
    if (frame.current !== null) cancelAnimationFrame(frame.current);
  }, []);
  return <button className={`button button-${variant} ${magnetic ? "button-magnetic" : ""} ${className}`.trim()}
    onPointerMove={handleMove} onPointerLeave={handleLeave} style={style} {...props} />;
}

export function PageHeader({ eyebrow, title, description, action }: {
  eyebrow: string;
  title: string;
  description: string;
  action?: ReactNode;
}) {
  const id = `${title.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)/g, "")}-title`;
  return (
    <div className="page-heading">
      <div><p className="eyebrow">{eyebrow}</p><h1 id={id}>{title}</h1><p className="lede">{description}</p></div>
      {action && <div className="page-heading-action">{action}</div>}
    </div>
  );
}

export function PanelHeading({ kicker, title, children }: { kicker?: string; title: string; children?: ReactNode }) {
  return <div className="panel-heading"><div>{kicker && <p className="section-kicker">{kicker}</p>}<h2>{title}</h2></div>{children}</div>;
}

export function Notice({ children, tone = "info", role }: { children: ReactNode; tone?: "info" | "success" | "warning" | "danger"; role?: "status" | "alert" }) {
  return <div className={`notice notice-${tone}`} role={role ?? (tone === "danger" ? "alert" : "status")}>{children}</div>;
}

export function MutationReviewNotice({ required, ready, onReviewed }: { required: boolean; ready: boolean; onReviewed: () => void }) {
  if (!required) return null;
  return <Notice tone="warning"><div className="mutation-review"><span><strong>Change outcome uncertain.</strong> The server may have saved the last request, and it was not retried. Review the refreshed records before submitting another change.</span>
    <Button variant="secondary" disabled={!ready} onClick={onReviewed}>I reviewed the current records</Button></div></Notice>;
}

export function StatusBadge({ children, tone = "neutral" }: { children: ReactNode; tone?: "neutral" | "success" | "warning" | "danger" | "blue" | "gold" }) {
  return <span className={`status-badge badge-${tone}`}>{children}</span>;
}

export function LoadState({ state, error, onRetry, empty, children }: {
  state: "loading" | "error" | "ready";
  error?: string;
  onRetry: () => void;
  empty?: boolean;
  children?: ReactNode;
}) {
  if (state === "loading") return <div className="state-block" role="status"><span className="spinner" aria-hidden="true" />Loading current records…</div>;
  if (state === "error") return <div className="state-block state-error" role="alert"><h3>Could not load current records</h3><p>{error}</p><Button onClick={onRetry}>Retry read</Button></div>;
  if (empty) return <div className="state-block state-empty"><span className="empty-symbol" aria-hidden="true">◇</span><h3>No matching records</h3><p>Create a record or change the current filter.</p></div>;
  return <>{children}</>;
}

export function Modal({ title, children, onClose, className = "" }: {
  title: string;
  children: ReactNode;
  onClose: () => void;
  className?: string;
}) {
  const root = useRef<HTMLDivElement>(null);
  const closeRef = useRef(onClose);
  useEffect(() => { closeRef.current = onClose; }, [onClose]);
  useEffect(() => {
    const previous = document.activeElement instanceof HTMLElement ? document.activeElement : null;
    const focusable = () => root.current?.querySelectorAll<HTMLElement>(
      'button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), a[href], [tabindex]:not([tabindex="-1"])',
    );
    focusable()?.[0]?.focus();
    const keydown = (event: KeyboardEvent) => {
      if (event.key === "Escape") { event.preventDefault(); closeRef.current(); return; }
      if (event.key !== "Tab") return;
      const elements = focusable();
      if (!elements?.length) { event.preventDefault(); return; }
      const first = elements[0];
      const last = elements[elements.length - 1];
      if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
      else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
    };
    document.addEventListener("keydown", keydown);
    return () => { document.removeEventListener("keydown", keydown); previous?.focus(); };
  }, []);

  return (
    <div className="modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}>
      <div ref={root} className={`modal-surface ${className}`.trim()} role="dialog" aria-modal="true" aria-labelledby="modal-title" tabIndex={-1}>
        <div className="modal-heading"><h2 id="modal-title">{title}</h2><Button variant="quiet" aria-label="Close dialog" onClick={onClose}>Close</Button></div>
        {children}
      </div>
    </div>
  );
}

export function DetailSheet({ title, children, onClose }: { title: string; children: ReactNode; onClose: () => void }) {
  return <Modal title={title} onClose={onClose} className="detail-sheet">{children}</Modal>;
}

export function FormField({ label, hint, children }: { label: string; hint?: string; children: ReactNode }) {
  return <div className="form-field"><label>{label}{children}</label>{hint && <small>{hint}</small>}</div>;
}

export function FieldError({ children }: { children: ReactNode }) {
  return <p className="field-error" role="alert">{children}</p>;
}

export function MetricCard({ label, value, detail, tone = "blue" }: {
  label: string;
  value: string | number;
  detail: string;
  tone?: "blue" | "gold" | "green" | "rose";
}) {
  const accent = { blue: "--navy", gold: "--gold", green: "--success", rose: "--danger" }[tone];
  const style = { "--metric-accent": `var(${accent})` } as CSSProperties;
  return <GlassPanel as="article" className="metric-card" tilt><p>{label}</p><strong style={style}>{value}</strong><small>{detail}</small></GlassPanel>;
}
