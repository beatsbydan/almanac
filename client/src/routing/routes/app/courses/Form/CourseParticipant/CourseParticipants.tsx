import React, { useState, useMemo } from 'react'
import { GenericObjectType, courseParticipantsType } from '../../../../../../utils/Types';
import { Pencil, X } from 'lucide-react';
import CourseParticipantForm from './CourseParticipantForm';
import Button from '../../../../../../ui/Button';
import UpsertModal from '../../../UpsertModal';

const CourseParticipants:React.FC<courseParticipantsType> = ({save, removeParticipant, updateParticipant, participants}) => {
    const [isOpen, setOpen] = useState<boolean>(false);
    const [selectedParticipant, setSelectedParticipant] = useState<GenericObjectType | null>(null);

    const selectParticipant = (e:GenericObjectType) => {
        setSelectedParticipant({...e})
        setOpen(true)
    }

    const noCourseParticipants = useMemo(() => {
        return participants.length === 0;
    }, [participants])

    return (
        <div className='w-full'>
            <div className='w-full flex items-center justify-between'>
                <p className='text-[13px] w-full'>Add Course Participants</p>
                <Button
                    processing={false}
                    onClick={()=>setOpen(true)}
                    styleClass='bg-bowen-blue hover:border-bowen-blue text-white font-semibold'
                    spinnerStyleClass='w-6 h-6 mx-auto'
                    delay={1.2}
                >
                    Add
                </Button>
            </div>
            <ul className={`w-full flex flex-wrap items-center gap-x-2 gap-y-1 mt-2`}>
                {
                    noCourseParticipants ? <li className=' w-full mx-auto text-center justify-center text-light-gray text-[13px] font-medium'>No participants added..</li>
                    :
                    participants.map((value: GenericObjectType, index: number) => {
                        return (
                            <li className="flex items-center gap-3 p-1 rounded-sm bg-black" key={index}>
                                <span className="text-[10px] text-white font-medium">{`${value?.level} lvl - ${value?.departments?.length} department(s).`}</span>
                                <div className='flex text-light-gray items-center gap-2'>
                                    <Pencil 
                                        size={15} 
                                        className='cursor-pointer' 
                                        onClick={()=>selectParticipant(value)}
                                    />
                                    <X 
                                        size={15} 
                                        className="text-light-gray cursor-pointer" 
                                        onClick={()=>removeParticipant(value)}
                                    />
                                </div>
                                
                            </li>
                        )
                    })
                }
            </ul>
            {isOpen &&
                <UpsertModal isOpen={isOpen} closeModal={()=>setOpen(false)}>
                    <CourseParticipantForm 
                        participant={selectedParticipant} 
                        save={save} 
                        update={updateParticipant} 
                        closeModal={()=>setOpen(false)}
                    />
                </UpsertModal>
            }
        </div>
    )
}

export default CourseParticipants