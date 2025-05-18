import React from 'react'
import { InputComponentRevealType } from '../../utils/Types'
import { motion } from 'framer-motion'

const TextInputRevealComponent:React.FC<InputComponentRevealType> = ({
    styleClass,  
    delay,
    type,
    name,
    value, 
    placeholder, 
    onChange
}) => {
    return (
        <motion.input
            name={name}
            type={type}
            placeholder={placeholder}
            value={value}
            onChange={onChange}
            className={styleClass}
            initial={{ opacity: 0, x: -50 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.3, ease: "easeOut", delay: delay }}
        />
    )
}

const TextAreaRevealComponent:React.FC<InputComponentRevealType> = React.memo(({
    styleClass,  
    delay,
    name,
    value, 
    placeholder, 
    onChange
}) => {
    return (
        <motion.textarea
            name={name} 
            id={name} 
            cols={40} 
            rows={6}
            placeholder={placeholder}
            value={value}
            onChange={onChange}
            className={styleClass}
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, ease: "easeOut", delay: delay }}
        />
    )
})

export const TextInputComponentReveal:React.FC<InputComponentRevealType> = React.memo((props) => {
    const { type } = props;
    
    const renderComponents = () => {
        switch(type){
            case "text":
                return <TextInputRevealComponent {...props}/>
            case "password":
                return <TextInputRevealComponent {...props}/>
            case "textarea":
                return <TextAreaRevealComponent {...props}/>
            default:
                return
        }
    }

    return (
        <React.Fragment>
            {renderComponents()}
        </React.Fragment>
    )
})