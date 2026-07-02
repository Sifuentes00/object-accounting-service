import { createBrowserRouter } from 'react-router-dom';
import Login from '@/pages/Login';
import Layout from '@/components/Layout';
import Objects from '@/pages/Objects';
import Customers from '@/pages/Customers';
import Employees from '@/pages/Employees';
import ObjectDetail from '@/pages/ObjectDetail';

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
      {
        path: 'customers',
        element: <Customers />,
      },
      {
        path: 'employees',
        element: <Employees />,
      },
      {
        path: 'objects/:id',
        element: <ObjectDetail />,
      },
    ],
  },
]);

export default router;
