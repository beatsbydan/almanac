import React, { ReactElement } from 'react'
import { ModalType } from '../../../utils/Types';
import { motion } from 'framer-motion'
import Modal from '../../../ui/Modal';

const UpsertModal:React.FC<ModalType> = ({isOpen, closeModal, children}) => {
    return (
        <Modal
            isOpen={isOpen}
            closeModal={closeModal}
        >
            <motion.div 
                className='w-[90%] max-w-[450px] rounded-2xl shadow-2xl relative bg-white p-6 left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2'
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.4, ease: "easeOut" }}
            >
                {React.isValidElement(children) ? React.cloneElement(children as ReactElement<{closeModal: () => void }>, {closeModal}) : children}
            </motion.div>
        </Modal>
    )
}

export default React.memo(UpsertModal)