import React, { useCallback, useState } from 'react'
import { ComponentReveal } from '../../../../ui/animations/ComponentAnimations';
import { HardDriveDownload, Pencil } from 'lucide-react';
import { ActiveTimeTableType } from '../../../../utils/Types';
import { useApi, useAuth } from '../../../../hooks/useContexts';
import { motion } from 'framer-motion'
import { alertSuccess, alertFailure } from '../../../../utils/Notifications';
import { AdminPermission, NotAdminPermission } from '../../../Permissions';
import { generateFileName } from '../../../../utils/Helpers';
import { ROLES } from '../../../../utils/Constants';
import Toggle from '../../../../ui/Toggle'
import Countdown from '../../../../ui/Countdown';
import ApiRoutes from '../../../../utils/ApiRoutes';
import logo from '../../../../assets/images/logo.svg'
import Spinner from '../../../../ui/Spinner';

const Active:React.FC<ActiveTimeTableType> = ({timetable, openModal, refetchMethod}) => {
    const [editView, setEditView] = useState<boolean>(false)
    const [downloadView, setDownloadView] = useState<boolean>(false)

    const [processingDownload, setProcessingDownload] = useState<boolean>(false)

    const { makeDownloadRequest, makePutRequest } = useApi();
    const { user, role } = useAuth();

    const changeTimetableStatus = useCallback(async () => {
        const { success, error, responseBody } = await makePutRequest(`${ApiRoutes.TIMETABLE.change_status + timetable?.code}`, {} ,{status: timetable?.canCreateSchedules ? "PAUSED" : "ENABLED"});
        if(success){
            alertSuccess(responseBody!.result.message)
            refetchMethod()
        }   
        if(error){
            alertFailure(error!.result.message)
        }
    }, [timetable])

    const deactivateTimeTable = useCallback(async () => {
        const { success, error, responseBody } = await makePutRequest(`${ApiRoutes.TIMETABLE.deactivate}`);
        if(success){
            alertSuccess(responseBody!.result.message)
            refetchMethod()
        }   
        if(error){
            alertFailure(error!.result.message)
        }
    }, [])

    const downloadTimeTable = useCallback(async () => {
        setProcessingDownload(true)
        const { success, error, responseBody } = await makeDownloadRequest(ApiRoutes.TIMETABLE.download + timetable?.code, {userId: user?.id});
        if(success){
            const url = window.URL.createObjectURL(new Blob([responseBody!]))
            const link = document.createElement('a');
            link.href = url;

            const isStudent = role === ROLES.STUDENT;

            link.setAttribute('download', generateFileName(isStudent, timetable!, user!));

            document.body.appendChild(link);
            link.click()

            document.body.removeChild(link);
            alertSuccess("Timetable Downloaded!")
        }   
        if(error){
            alertFailure(error!.result.message)
        }
        setProcessingDownload(false)
    }, [])

    return (
        <div className='w-full max-w-[700px] mx-auto'>
            <div className='mx-auto pt-12 flex items-start relative '>
                <div className='w-full'>
                    <h1 className='text-7xl font-semibold'>{timetable?.session}</h1>
                    <p className='font-semibold text-light-gray pt-4'>{timetable?.semester} SEMESTER</p>                        
                    <AdminPermission>
                        <div className='w-full max-w-[100px] flex flex-col text-center mt-12'>
                            <p className='text-sm mb-1 font-semibold text-light-gray'>{timetable?.canCreateSchedules ? "Disable" : "Enable"}</p>
                            <Toggle
                                styleClass='w-9 aspect-square'
                                action={changeTimetableStatus}
                                color="bg-bowen-blue"
                                condition={timetable?.canCreateSchedules}
                            />
                        </div>
                        <div className='w-full max-w-[100px] flex flex-col text-center mt-6'>
                            <p className='text-sm mb-1 font-semibold text-light-gray'>{timetable?.active ? "Deactivate" : "Activate"}</p>
                            <Toggle
                                styleClass='w-9 aspect-square'
                                action={deactivateTimeTable}
                                color="bg-red-600"
                                condition={timetable?.active}
                            />
                        </div>
                    </AdminPermission>
                </div>
                <div className='w-full flex gap-12 items-center mt-6 justify-end'>
                    <AdminPermission>
                        <ComponentReveal
                            styleClass='relative'
                            delay={1.0}
                            type="container"
                        >
                            <Pencil 
                                onMouseEnter={()=>setEditView(true)} 
                                onMouseLeave={()=>setEditView(false)} 
                                size={30} 
                                cursor={'pointer'} 
                                onClick={openModal}
                            />
                            { editView &&
                                <div className="absolute -left-4 mt-5 flex items-center justify-center">
                                <motion.div
                                    initial={{ opacity: 0, scale: 0.8, y: 40 }}
                                    animate={{ opacity: 1, scale: 1, y: 0 }}
                                    transition={{ duration: 0.3, ease: "easeOut" }}
                                    className="relative bg-black px-4 py-2 rounded-md shadow-lg"
                                >
                                    <div className="absolute left-1/2 -translate-x-1/2 -top-[15px] rotate-90 border-y-12 border-r-12 border-l-0 border-solid border-transparent border-r-black"/>
                                    <p className='text-white text-sm'>Edit</p>
                                </motion.div>
                                </div>
                            }
                        </ComponentReveal>
                    </AdminPermission>
            
                    <ComponentReveal
                        styleClass='relative'
                        delay={0.5}
                        type="container"
                    >
                        {!processingDownload ? 
                            <HardDriveDownload 
                                size={30} 
                                onMouseEnter={()=>setDownloadView(true)} 
                                onMouseLeave={()=>setDownloadView(false)}  
                                cursor={'pointer'} 
                                onClick={downloadTimeTable}
                            />
                            :
                            <Spinner spinnerStyleClass='w-[30px] aspect-square'/>
                        }
                        { downloadView &&
                            <div className="absolute -left-8 mt-5 flex items-center justify-center">
                                <motion.div
                                    initial={{ opacity: 0, scale: 0.8, y: 40 }}
                                    animate={{ opacity: 1, scale: 1, y: 0 }}
                                    transition={{ duration: 0.3, ease: "easeOut" }}
                                    className="relative bg-black px-4 py-2 rounded-md shadow-lg"
                                >
                                    <div className="absolute left-1/2 -translate-x-1/2 -top-[15px] rotate-90 border-y-12 border-r-12 border-l-0 border-solid border-transparent border-r-black"/>
                                    <p className='text-white text-sm'>Download</p>
                                </motion.div>
                            </div>
                        }
                    </ComponentReveal>
                </div>
                <AdminPermission>
                    <Countdown nextDate={timetable?.creationEndDate || new Date()} delay={0.4}/>
                </AdminPermission>
            </div>
                <NotAdminPermission>
                    <div className=' w-full flex items-center justify-between mt-12'>
                        <ComponentReveal
                            type='image'
                            src={logo} 
                            alt="" 
                            styleClass="w-36 h-36 mx-auto"
                            delay={0.2}
                        />
                        <p className=''>
                            Download timetable for <span className='font-bold'>{user?.level ? `${user?.level} level` : user?.id}</span> <span className='font-bold'>{user?.department ? `in ${user?.department} department` : ""}.</span>
                        </p>
                    </div>
                </NotAdminPermission>
        </div>
    )
}

export default Active