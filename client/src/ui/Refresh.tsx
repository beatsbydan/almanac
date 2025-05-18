import React from 'react'
import Spinner from './Spinner'
import logo from '../assets/images/logo.svg'
import { ComponentReveal } from './animations/ComponentAnimations'

const Refresh:React.FC = () => {
    return (
        <div className='w-full h-screen fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 flex flex-col items-center justify-center gap-5 bg-white py-12 px-8 shadow-2xl rounded-lg'>
            <ComponentReveal
                type='image'
                src={logo} 
                alt="" 
                styleClass="w-50 aspect-square"
                delay={0.2}
            />
            <Spinner spinnerStyleClass='w-10 aspect-square'/>
        </div>
    )
}

export default Refresh