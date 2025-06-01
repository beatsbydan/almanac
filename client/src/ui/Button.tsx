import React from 'react'
import { ButtonType } from '../utils/Types'
import { motion } from 'framer-motion'
import Spinner from './Spinner'

const Button:React.FC<ButtonType> = ({
  processing, 
  children, 
  onClick, 
  spinnerStyleClass, 
  styleClass, 
  delay,
  type
}) => {
  return (
    <motion.button
      type={type}
      className={"rounded-lg p-3 w-full cursor-pointer hover:bg-transparent hover:text-bowen-blue transition-colors ease-in-out border-1 border-transparent  " + styleClass + ` ${processing ? "bg-transparent" : ""}`}
      onClick={onClick}
      disabled={processing}
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6, ease: "easeOut", delay: delay }}
    >
      {processing ? <Spinner spinnerStyleClass={spinnerStyleClass}/> : children}
    </motion.button>
  )
}

export default Button