import React, { useState, useCallback, useEffect } from 'react'
import { motion } from 'framer-motion'
import { GenericObjectType, SuggestionModalType } from '../../../../utils/Types'
import { X, RefreshCcw } from 'lucide-react'
import { useApi } from '../../../../hooks/useContexts'
import Table from '../../../../ui/table/Table'
import ApiRoutes from '../../../../utils/ApiRoutes'

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

    const [suggestions, setSuggestions] = useState<GenericObjectType[]>([])
    const [loading, setLoading] = useState<boolean>(false)

    const getSuggestions = useCallback(async () => {
        setLoading(true)
        
        const { success, responseBody } = await makeGetRequest(ApiRoutes.SCHEDULE.suggestions);
        if(success){
            console.log(responseBody)
            setSuggestions([...responseBody!.result])
        }
        setLoading(false)
    }, [])

    const useSuggestion = useCallback((e:GenericObjectType) => {
        saveSuggestion({...e, course: {}})
    }, [])

    useEffect(() => {
        getSuggestions()
    },[getSuggestions])

    return (
        <motion.div 
            className='w-[95%] h-[95vh] rounded-2xl shadow-2xl relative bg-white p-6 left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2'
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.2, ease: "easeOut" }}
        >
            <X
                className="cursor-pointer top-2 right-2 absolute text-light-gray"
                onClick={closeModal}
            />
            <RefreshCcw 
                className='cursor-pointer top-2 right-12 absolute text-light-gray'
                onClick={getSuggestions}
            />
            <Table
                displayKeys={["courseCodesString", "classroomsString", "day", "time"]}
                emptyText='schedules'
                listStyle='grid-cols-[0.2fr_0.5fr_0.5fr_0.4fr_0.5fr_0.4fr]'
                headers={headers}
                list={suggestions}
                loading={loading}
                isFilterable={true}
                canEdit={true}
                canDelete={false}
                delay={0.4}
                tableHeight='h-[77vh]'
                selectEntity={useSuggestion}
            />
        </motion.div>
    )
}

export default Suggestions