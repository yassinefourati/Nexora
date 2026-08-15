export default function RootFallback({ error }: { error: unknown }) {
  const message = error instanceof Error ? error.message : String(error);
  return (
    <div style={{ height:'100vh', display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', fontFamily:'sans-serif', padding:'2rem', textAlign:'center' }}>
      <h2 style={{ color:'#d32f2f', marginBottom:'0.5rem' }}>Application failed to load</h2>
      <p style={{ color:'#666', marginBottom:'1rem', maxWidth:480 }}>{message}</p>
      <button onClick={() => window.location.reload()} style={{ padding:'8px 24px', background:'#1976d2', color:'#fff', border:'none', borderRadius:6, cursor:'pointer', fontSize:'1rem' }}>Reload</button>
    </div>
  );
}
