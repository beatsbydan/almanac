import React, { useState } from 'react'
import { useApi, useAuth, useNav } from '../../hooks/useContexts'
import { ComponentReveal } from '../../ui/animations/ComponentAnimations'
import { LogOutIcon } from 'lucide-react'
import { useNavigate, useLocation } from 'react-router-dom'
import { motion } from 'framer-motion'
import ApiRoutes from '../../utils/ApiRoutes'
import Spinner from '../../ui/Spinner'

const Navbar:React.FC = () => {
    const { currentPage } = useNav();
    const { user, logout } = useAuth();
    const { makeGetRequest } = useApi();

    const location = useLocation();

    const navigate = useNavigate();

    const [processing, setProcessing] = useState<boolean>(false)
    const [logoutView, setLogoutView] = useState<boolean>(false)

    const logOut = async () => {
        setProcessing(true)

        await makeGetRequest(ApiRoutes.AUTH.logout);
        logout()
        navigate("/login", {replace:true, state:{from: location}})

        setProcessing(false)
    }

    return (
        <div className="w-full py-2 px-[3%] flex justify-between items-center border-b-1 border-b-light-gray">
            <ComponentReveal
                type='text'
                styleClass="text-[24px] font-semibold mt-2 "
                delay={0.3}
            >
                {currentPage}
            </ComponentReveal>
            <div className='flex items-center gap-12 relative'>
                <p className="text-light-gray font-medium text-[14px]">USER ID: <span className='font-bold text-light-black'>{user?.id}</span></p>
                {processing ? 
                    <Spinner spinnerStyleClass='w-[18px] aspect-square'/> 
                    : 
                    <LogOutIcon 
                        size={18} 
                        cursor={"pointer"}
                        className='text-red-600' 
                        onClick={logOut} 
                        onMouseEnter={()=>setLogoutView(true)}
                        onMouseLeave={()=>setLogoutView(false)}
                    />
                }
                { logoutView &&
                    <div className="absolute mt-5 top-7 -right-7 flex items-center justify-center">
                        <motion.div
                            initial={{ opacity: 0, scale: 0.8, y: 40 }}
                            animate={{ opacity: 1, scale: 1, y: 0 }}
                            transition={{ duration: 0.3, ease: "easeOut" }}
                            className="relative bg-red-600 px-4 py-2 rounded-md shadow-lg"
                        >
                            <div className="absolute left-1/2 -translate-x-1/2 -top-[15px] rotate-90 border-y-12 border-r-12 border-l-0 border-solid border-transparent border-r-red-600"/>
                            <p className='text-white text-sm'>Logout</p>
                        </motion.div>
                    </div>
                }
            </div>
        </div>
    )
}

export default Navbar