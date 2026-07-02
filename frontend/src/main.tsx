import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { EditModeProvider } from './context/EditModeContext'
import { ErrorProvider } from './context/ErrorContext'
import './index.css'
import router from './router'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ErrorProvider>
      <AuthProvider>
        <EditModeProvider>
          <RouterProvider router={router} />
        </EditModeProvider>
      </AuthProvider>
    </ErrorProvider>
  </StrictMode>,
)
