import { createBrowserRouter } from 'react-router-dom';
import Login from '@/pages/Login';
import Layout from '@/components/Layout';
import Objects from '@/pages/Objects';

const router = createBrowserRouter([
  {
    path: '/',
    element: <Login />,
  },
  {
    path: '/main',
    element: <Layout />,
    children: [
      {
        index: true,
        element: <Objects />,
      },
    ],
  },
]);

export default router;
