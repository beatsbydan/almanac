import React, { ReactNode } from 'react'
import Sidebar from '../../navigation/Sidebar'
import Navbar from '../../navigation/Navbar'

const Card:React.FC<{children: ReactNode}> = ({children}) => {
    return (
        <div className='relative w-full'>
            <Sidebar/>
            <div className='w-[calc(100%-100px)] h-screen relative float-right'>
                <div className='w-[97%] h-[94%] shadow-2xl rounded-lg bg-white absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 float-right px-5'>
                    <Navbar/>
                    <div className='w-full h-[85%]'>
                        {children}
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Card