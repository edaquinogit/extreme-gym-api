import { AppProvider } from './app/providers/AppProvider'
import { AppRoutes } from './app/routes/AppRoutes'

function App() {
  return (
    <AppProvider>
      <AppRoutes />
    </AppProvider>
  )
}

export default App
