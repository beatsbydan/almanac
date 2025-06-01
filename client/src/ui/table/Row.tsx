import React, { useState } from 'react'
import { ComponentReveal } from '../animations/ComponentAnimations'
import { Pencil, Trash2 } from 'lucide-react'
import { motion } from 'framer-motion'
import { useApi } from '../../hooks/useContexts'
import { alertSuccess, alertFailure } from '../../utils/Notifications'
import { TableRowType } from '../../utils/Types'
import Spinner from '../Spinner'

const Row:React.FC<TableRowType> = ({
    index,
    listStyle,
    entity,
    displayKeys,
    canEdit,
    canView,
    canDelete,
    deleteApiRoute,
    selectEntity,
    refetchMethod
}) => {
    const [editView, setEditView] = useState<boolean>(false)
    const [deleteView, setDeleteView] = useState<boolean>(false)
    const [processingDelete, setProcessingDelete] = useState<boolean>(false)

    const { makeDeleteRequest } = useApi()

    const deleteEntity = async() => {
        setProcessingDelete(true)
        const { success, error, responseBody } = await makeDeleteRequest(`${deleteApiRoute + entity?.code}`)

        if(success){
            console.log(responseBody)
            alertSuccess(responseBody!.result.message)
            refetchMethod!()
        }
        if(error){
            alertFailure(error!.result.message)
        }
        setProcessingDelete(false)
    }

    return (
        <ComponentReveal
            type="list"
            key={index}
            delay={(index + 1) * 0.2}
            styleClass={'w-full grid py-3 ' + listStyle }
        >
            <p className='font-bold text-center'>{index + 1}</p>
            {displayKeys?.map((value: string, index: number) => {
                return (
                    <p key={index} className='text-center break-all'>{entity[value]}</p>
                )
            })}
            <div className='text-center flex items-center justify-center gap-5'>
                {(canEdit || canView) && 
                    <div className='relative'>
                        <Pencil 
                            size={18} 
                            className='cursor-pointer' 
                            onClick={()=>selectEntity!(entity)}
                            onMouseEnter={()=>setEditView(true)} 
                            onMouseLeave={()=>setEditView(false)}
                        />
                        { editView &&
                            <div className="absolute z-20 -left-5 mt-5 flex items-center justify-center">
                                <motion.div
                                    initial={{ opacity: 0, scale: 0.8, y: 40 }}
                                    animate={{ opacity: 1, scale: 1, y: 0 }}
                                    transition={{ duration: 0.3, ease: "easeOut" }}
                                    className="relative bg-black px-4 py-2 rounded-md shadow-lg"
                                >
                                    <div className="absolute left-1/2 -translate-x-1/2 -top-[15px] rotate-90 border-y-12 border-r-12 border-l-0 border-solid border-transparent border-r-black"/>
                                    <p className='text-white text-sm'>{canView ? "Use" : "Edit"}</p>
                                </motion.div>
                            </div>
                        }
                    </div>
                }
                {canDelete && 
                    <div className='relative'>
                        {
                            processingDelete ? <Spinner spinnerStyleClass='w-[18px] aspect-square'/> 
                            :
                            <Trash2 
                                size={18} 
                                className='cursor-pointer text-red-600' 
                                onClick={deleteEntity}
                                onMouseEnter={()=>setDeleteView(true)} 
                                onMouseLeave={()=>setDeleteView(false)}
                            />
                        }
                        { deleteView &&
                            <div className="absolute -left-7 z-20 mt-5 flex items-center justify-center">
                                <motion.div
                                    initial={{ opacity: 0, scale: 0.8, y: 40 }}
                                    animate={{ opacity: 1, scale: 1, y: 0 }}
                                    transition={{ duration: 0.3, ease: "easeOut" }}
                                    className="relative bg-red-600 px-4 py-2 rounded-md shadow-lg"
                                >
                                    <div className="absolute left-1/2 -translate-x-1/2 -top-[15px] rotate-90 border-y-12 border-r-12 border-l-0 border-solid border-transparent border-r-red-600"/>
                                    <p className='text-white text-sm'>Delete</p>
                                </motion.div>
                            </div>
                        }
                    </div>
                }
            </div>
        </ComponentReveal>
    )
}

export default Row