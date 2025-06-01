import React, { useState, useCallback, useEffect } from 'react'
import { motion } from 'framer-motion'
import { GenericObjectType, SuggestionModalType } from '../../../../utils/Types'
import { X, RefreshCcw, ListFilterIcon as Filter } from 'lucide-react'
import { useApi } from '../../../../hooks/useContexts'
import Table from '../../../../ui/table/Table'
import ApiRoutes from '../../../../utils/ApiRoutes'
import { ComponentReveal } from '../../../../ui/animations/ComponentAnimations'
import UpsertModal from '../UpsertModal'
import { SuggestionFilter } from '../../../../ui/Filters'

const Suggestions:React.FC<SuggestionModalType> = ({closeModal, saveSuggestion}) => {
    const headers:string[] = [
        'S/N',
        'CLASSROOM',
        'CAPACITY',
        'DAY',
        'TIME',
        'ACTIONS'
    ]

    const { makeGetRequest } = useApi()

    const [filterParams, setFilterParams] = useState<GenericObjectType>({
        day: null,
        startTime: null,
        duration: null,
        college: null,
        classSize: null
    })
    const [filterOpen, setFilterOpen] = useState<boolean>(false)
    const [filterView, setFilterView] = useState<boolean>(false)
    const [regenerateView, setRegenerateView] = useState<boolean>(false)

    const [suggestions, setSuggestions] = useState<GenericObjectType[]>([])
    const [loading, setLoading] = useState<boolean>(false)

    const getSuggestions = useCallback(async () => {
        setLoading(true)
        
        const { success, responseBody } = await makeGetRequest(ApiRoutes.SCHEDULE.suggestions, filterParams);
        if(success){
            const updatedSuggestions: GenericObjectType[] = []
            for(let suggestion of [...responseBody!.result]){
                const newSuggestion = {
                    ...suggestion, 
                    classRoomName: suggestion?.classRoom?.className,
                    classRoomCapacity: suggestion?.classRoom?.classSize
                }
                updatedSuggestions.push(newSuggestion)
            }
            setSuggestions([...updatedSuggestions])
        }
        setLoading(false)
    }, [filterParams])

    const useSuggestion = useCallback((e:GenericObjectType) => {
        saveSuggestion(
            {...e, 
                course: {}, 
                classrooms: [e?.classRoom], 
                recurringDay: e?.day,
                timeDuration: e?.duration
            }
        )
    }, [])

    const openFilterModal = useCallback(() => {
        setFilterOpen(true)
    }, [])

    useEffect(() => {
        getSuggestions()
    },[getSuggestions])

    return (
        <motion.div 
            className='w-[95%] h-[95vh] rounded-2xl shadow-2xl relative bg-white p-4 left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2'
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.2, ease: "easeOut" }}
        >
            <div className='w-full flex items-center justify-between -mb-1'>
                <div className='flex items-center gap-5'>
                    <ComponentReveal
                        styleClass='w-full'
                        delay={0.6}
                        type="container"
                    >
                        <Filter 
                            onMouseEnter={()=>setFilterView(true)} 
                            onMouseLeave={()=>setFilterView(false)} 
                            size={30} 
                            cursor={'pointer'} 
                            onClick={openFilterModal}
                        />
                        { filterView &&
                            <div className="absolute mt-5 left-[1px] flex items-center justify-center">
                                <motion.div
                                    initial={{ opacity: 0, scale: 0.8, y: 40 }}
                                    animate={{ opacity: 1, scale: 1, y: 0 }}
                                    transition={{ duration: 0.3, ease: "easeOut" }}
                                    className="relative bg-black px-4 py-2 rounded-md shadow-lg"
                                >
                                    <div className="absolute left-1/2 -translate-x-1/2 -top-[15px] rotate-90 border-y-12 border-r-12 border-l-0 border-solid border-transparent border-r-black"/>
                                    <p className='text-white text-sm'>Filter</p>
                                </motion.div>
                            </div>
                        }
                    </ComponentReveal>
                    <ComponentReveal
                        styleClass='w-full'
                        delay={0.8}
                        type="container"
                    >
                        <RefreshCcw 
                            onClick={getSuggestions}
                            size={30}
                            cursor={'pointer'} 
                            onMouseEnter={()=>setRegenerateView(true)} 
                            onMouseLeave={()=>setRegenerateView(false)}
                        />
                        { regenerateView &&
                            <div className="absolute left-7 mt-5 flex items-center justify-center">
                                <motion.div
                                    initial={{ opacity: 0, scale: 0.8, y: 40 }}
                                    animate={{ opacity: 1, scale: 1, y: 0 }}
                                    transition={{ duration: 0.3, ease: "easeOut" }}
                                    className="relative bg-black px-4 py-2 rounded-md shadow-lg"
                                >
                                    <div className="absolute left-1/2 -translate-x-1/2 -top-[15px] rotate-90 border-y-12 border-r-12 border-l-0 border-solid border-transparent border-r-black"/>
                                    <p className='text-white text-sm'>Regenerate</p>
                                </motion.div>
                            </div>
                        }
                    </ComponentReveal>
                </div>
                <X
                    className="cursor-pointer text-light-gray"
                    onClick={closeModal}
                />
            </div>
            <Table
                displayKeys={["classRoomName", "classRoomCapacity", "day", "time"]}
                emptyText='suggestions'
                listStyle='grid-cols-[0.2fr_0.5fr_0.5fr_0.4fr_0.5fr_0.4fr]'
                headers={headers}
                list={suggestions}
                loading={loading}
                isFilterable={false}
                canEdit={false}
                canView={true}
                canDelete={false}
                delay={0.4}
                tableHeight='h-[74vh]'
                selectEntity={useSuggestion}
            />
            {filterOpen && 
                <UpsertModal isOpen={filterOpen} closeModal={()=>setFilterOpen(false)}>
                    <SuggestionFilter setFilter={setFilterParams} closeModal={()=>setFilterOpen(false)}/>
                </UpsertModal>
            }
        </motion.div>
    )
}

export default Suggestions