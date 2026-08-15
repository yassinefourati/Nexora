import { Box, Typography, Button, Paper } from '@mui/material';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@/core/router/routes';

export default function Unauthorized() {
  const navigate = useNavigate();
  return (
    <Box component="main" sx={{ height:'100vh', display:'flex', alignItems:'center', justifyContent:'center', bgcolor:'background.default' }}>
      <Paper elevation={3} sx={{ p:6, borderRadius:3, textAlign:'center', maxWidth:420 }}>
        <LockOutlinedIcon sx={{ fontSize:64, color:'text.disabled', mb:2 }} />
        <Typography variant="h4" component="h1" fontWeight={700} mb={1}>Access Denied</Typography>
        <Typography variant="body1" color="text.secondary" mb={3}>
          You don't have permission to view this page. Contact your administrator if you think this is a mistake.
        </Typography>
        <Button variant="contained" onClick={() => navigate(ROUTES.HOME)}>Back to Dashboard</Button>
        <Button sx={{ ml:1 }} onClick={() => navigate(-1)}>Go Back</Button>
      </Paper>
    </Box>
  );
}
