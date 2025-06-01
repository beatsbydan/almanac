import React, { useState, useEffect, useCallback } from 'react'
import { useApi, useNav } from '../../../../hooks/useContexts'
import { GenericObjectType } from '../../../../utils/Types';
import { ComponentReveal } from '../../../../ui/animations/ComponentAnimations';
import Card from '../Card'
import ApiRoutes from '../../../../utils/ApiRoutes';
import Form from './Form';
import UpsertModal from '../UpsertModal';
import Active from './Active';
import { AdminPermission } from '../../../Permissions';
import Spinner from '../../../../ui/Spinner';

const Timetables:React.FC = () => {
    const { changeCurrentPage } = useNav();
    const { makeGetRequest } = useApi();

    const [processing,  setProcessing] = useState<boolean>(false);
    const [open, setOpen] = useState<boolean>(false)

    const [activeTimetable, setActiveTimetable] = useState<GenericObjectType | undefined>(undefined)

    const openModal = useCallback(() => {
        setOpen(true)
    }, [])

    const closeModal = useCallback(() => {
        setOpen(false)
    }, [])

    const getActiveTimeTable = useCallback(async () => {
        setProcessing(true)
        const { success, responseBody } = await makeGetRequest(ApiRoutes.TIMETABLE.get_active);
        if(success){
            setActiveTimetable(responseBody!.result)
        }   
        setProcessing(false)
    }, [])

    useEffect(()=>{
        getActiveTimeTable()
    },[getActiveTimeTable])

    useEffect(() => { 
        changeCurrentPage("Timetables")
    },[changeCurrentPage])
    
    return (
        <Card>
            <div className='w-full h-full relative'>
                {
                    processing ? <Spinner spinnerStyleClass='w-15 aspect-square absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2'/> 
                    :
                    activeTimetable ? 
                        <Active timetable={activeTimetable} openModal={openModal} refetchMethod={getActiveTimeTable}/> 
                    : 
                    <div className='flex flex-col justify-center items-center gap-5 pt-34'>
                        <p className='text-light-gray font-medium'>No active timetable available</p>
                        <AdminPermission>
                            <ComponentReveal
                                styleClass=''
                                delay={0.2}
                                type="container"
                            >
                                <button 
                                    type="button" 
                                    className='cursor-pointer flex items-center gap-3 text-[16px] font-semibold w-full max-w-[180px] py-[10px] rounded-lg px-4 text-white hover:text-bowen-blue transition-colors duration-500 ease-out border-1 border-transparent hover:border-bowen-blue hover:bg-transparent bg-bowen-blue'
                                    onClick={openModal}
                                >
                                    Create <span className='text-[24px] font-bold'>+</span>
                                </button>
                            </ComponentReveal>
                        </AdminPermission>
                    </div>
                }  
                {open && 
                    <UpsertModal isOpen={open} closeModal={closeModal}>
                        <Form inputValues={activeTimetable} closeModal={closeModal} refetchMethod={getActiveTimeTable}/>
                    </UpsertModal>
                }
            </div>    
        </Card>
    )
}

export default Timetables