import React from 'react'
import { motion } from 'framer-motion'
import { ComponentRevealType } from '../../utils/Types'

export const ComponentReveal:React.FC<ComponentRevealType> = React.memo((props) => {
    const renderComponents = () => {
        const { type } = props;
        switch(type){
            case "text":
                return <TextRevealComponent {...props}/>
            case "image":
                return <ImageRevealComponent {...props}/>
            case "list":
                return <ListItemRevealComponent {...props}/>
            case "container":
                return <ContainerRevealComponent {...props}/>
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

const TextRevealComponent:React.FC<ComponentRevealType> = React.memo(({
    styleClass, 
    delay, 
    children, 
}) => {
    return (
        <motion.p
            className={styleClass}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, ease: "easeOut", delay: delay }}
        >
            {children}
        </motion.p>
    )
})

const ListItemRevealComponent:React.FC<ComponentRevealType> = React.memo(({
    styleClass, 
    delay, 
    children, 
}) => {
    return (
        <motion.li
            className={styleClass}
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, ease: "easeOut", delay: delay }}
        >
            {children}
        </motion.li>
    )
})

const ContainerRevealComponent:React.FC<ComponentRevealType> = React.memo(({
    styleClass, 
    delay, 
    children, 
}) => {
    return (
        <motion.div
            className={styleClass}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, ease: "easeOut", delay: delay }}
        >
            {children}
        </motion.div>
    )
})

const ImageRevealComponent:React.FC<ComponentRevealType> = React.memo(({
    styleClass, 
    delay, 
    src, 
    alt
}) => {
    return (
        <motion.img
            src={src}
            alt={alt}
            className={styleClass}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, ease: "easeOut", delay: delay }}
        />
    )
})