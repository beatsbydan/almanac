import React, {useEffect, useMemo} from 'react'
import { ModalType } from '../utils/Types'
import ReactDOM from 'react-dom'

const Modal:React.FC<ModalType> = ({isOpen, closeModal, children}) => {
    const modal = useMemo(() => document.createElement("div"), []);
    useEffect(() => {
        document.body.appendChild(modal)
        return () => {
            document.body.removeChild(modal)
        }
    },[modal])

    return isOpen ? 
    ReactDOM.createPortal(
        <div className="w-full h-[100vh] top-0 fixed z-30">
            <div className="w-full h-full fixed top-0 bg-sidebar blur-sm backdrop-blur-[1px] cursor-pointer" onClick={closeModal}/>
            {children}
        </div>,
        modal
    )
    : null

}

export default React.memo(Modal)