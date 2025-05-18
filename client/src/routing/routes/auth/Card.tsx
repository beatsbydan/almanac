import React from 'react'
import { AuthCardType } from '../../../utils/Types'
import { motion } from 'framer-motion'
import { ComponentReveal as AuthCardComponentReveal } from '../../../ui/animations/ComponentAnimations'
import logo from '../../../assets/images/logo.svg'
import Scroll from '../../../ui/Scroll'

const Card:React.FC<AuthCardType> = ({
    children,
    header,
    subHeader
}) => {

    return (
        <motion.div 
            className='w-[90%] max-w-[450px] rounded-2xl shadow-2xl relative bg-white p-6 left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2'
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.4, ease: "easeOut" }}
        >
            <motion.div className='w-full flex flex-col items-center '>
                <AuthCardComponentReveal
                    type='image'
                    src={logo} 
                    alt="" 
                    styleClass="w-15 h-15"
                    delay={0.2}
                />
                <AuthCardComponentReveal
                    type='text'
                    styleClass="text-[24px] text-center font-semibold mt-2 "
                    delay={0.3}
                >
                    {header}
                </AuthCardComponentReveal>
                <AuthCardComponentReveal
                    type='text'
                    styleClass="text-light-gray text-[16px] mt-2 text-center"
                    delay={0.4}
                >
                    {subHeader}
                </AuthCardComponentReveal>
            </motion.div>
            <div className='w-full h-[calc(100%-160px)]'>    
                <Scroll>
                    {children}
                </Scroll>
            </div>
        </motion.div>
    )
}

export default Card