import React, { useState, useCallback, useEffect } from 'react'
import { GenericObjectType } from '../../../../utils/Types'
import { useApi } from '../../../../hooks/useContexts'
import Table from '../../../../ui/table/Table';
import ApiRoutes from '../../../../utils/ApiRoutes';

const Admin:React.FC = () => {
    const headers:string[] = [
        'S/N',
        'COURSE CODE(S)',
        'CLASSROOM(S)',
        'DAY',
        'TIME'
    ]
    
    const { makeGetRequest } = useApi();

    const [schedules, setSchedules] = useState<GenericObjectType[]>([])
    const [loading, setLoading] = useState<boolean>(false)

    const getSchedules = useCallback(async () => {
        setLoading(true)
        
        const { success, responseBody } = await makeGetRequest(ApiRoutes.SCHEDULE.get);
        if(success){
            const updatedSchedules = []
            for(let schedule of responseBody!.result){
                const timeSplit = schedule?.time?.split(" - ");
                const timePeriod = parseFloat(timeSplit[1].split(":00")[0]) - parseFloat(timeSplit[0].split(":00")[0])
        
                updatedSchedules.push({
                ...schedule,
                courseCodesString: schedule?.course?.courseCodes?.join(", "),
                classroomsString: schedule?.classrooms?.map((room: GenericObjectType) => room?.className).join(", "),
                timeDuration: `${timePeriod} hours`
                })
            }
            setSchedules([...updatedSchedules])
        }
        setLoading(false)
    }, [])
    
    useEffect(() => {
        getSchedules()
    }, [getSchedules])
    
    return (
        <Table
            emptyText='schedules'
            displayKeys={["courseCodesString", "classroomsString", "day", "time"]}
            listStyle='grid-cols-[0.2fr_0.8fr_0.8fr_0.4fr_0.5fr]'
            headers={headers}
            tableHeight='h-[61.5vh]'
            list={schedules}
            loading={loading}
        />
    )
}

export default Admin