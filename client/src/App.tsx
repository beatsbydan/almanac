import React from 'react'
import { ToastContainer, Bounce } from 'react-toastify'
import Provider from './contexts/Provider'
import Routes from './routing/Routes'
import useMobile from './hooks/useMobile'
import Refresh from './ui/Refresh'
import './App.css'

const App:React.FC = () => {
  const isMobile = useMobile();
  return (
    <Provider>
      <div className='app'>
        {isMobile ? <Refresh/> : <Routes/>}
        <ToastContainer
          position="top-center"
          autoClose={3000}
          hideProgressBar={false}
          newestOnTop={false}
          closeOnClick={false}
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
          theme="light"
          transition={Bounce}
        />
      </div>
    </Provider>
  )
}

export default App