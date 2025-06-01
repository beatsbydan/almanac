import React, { useEffect, useState, useCallback } from 'react'
import { useNav, useApi, useAuth } from '../../../../hooks/useContexts'
import { ComponentReveal } from '../../../../ui/animations/ComponentAnimations';
import { GenericObjectType } from '../../../../utils/Types';
import { ScheduleFilter } from '../../../../ui/Filters';
import { motion } from 'framer-motion'
import { ListFilterIcon as Filter } from 'lucide-react'
import Card from '../Card'
import UpsertModal from '../UpsertModal';
import Form from './Form';
import Table from '../../../../ui/table/Table';
import ApiRoutes from '../../../../utils/ApiRoutes';
import Suggestions from './Suggestions';
import Modal from '../../../../ui/Modal';

const Schedules:React.FC = () => {
  const headers:string[] = [
    'S/N',
    'COURSE CODE(S)',
    'CLASSROOM(S)',
    'DAY',
    'TIME',
    'ACTIONS'
  ]
  
  const { changeCurrentPage } = useNav();
  const { makeGetRequest } = useApi();
  const { user } = useAuth()
  
  const [filterParams, setFilterParams] = useState<GenericObjectType>({
    userId: user?.id,
    day: null
  })
  const [inputValues, setInputValues] = useState<GenericObjectType | undefined>(undefined)
  const [suggestion, setSuggestion] = useState<GenericObjectType | undefined>(undefined)
  const [schedules, setSchedules] = useState<GenericObjectType[]>([])
  
  const [filterOpen, setFilterOpen] = useState<boolean>(false)
  const [formOpen, setFormOpen] = useState<boolean>(false)
  const [suggestionOpen, setSuggestionOpen] = useState<boolean>(false)
  const [filterView, setFilterView] = useState<boolean>(false)
  const [loading, setLoading] = useState<boolean>(false)

  const getSchedules = useCallback(async () => {
    setLoading(true)
    
    const { success, responseBody } = await makeGetRequest(ApiRoutes.SCHEDULE.get, filterParams);
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
  }, [filterParams])

  const selectSchedule = useCallback((e:GenericObjectType) => {
    const selectedSchedule = {
      code: e?.code,
      recurringDay: e?.day,
      startTime: e?.time?.split(" - ")[0],
      course: e?.course,
      classrooms: [...e?.classrooms],
      timeDuration: e?.timeDuration
    }
    setInputValues({...selectedSchedule})
    setFormOpen(true)
  }, [])

  const openFormModal = useCallback(() => {
    setInputValues(undefined)
    setFormOpen(true)
  }, [])

  const openFilterModal = useCallback(() => {
    setFilterOpen(true)
  }, [])

  const openSuggestionModal = useCallback(() => {
    setSuggestionOpen(true)
  }, [])

  const closeModal = useCallback(() => {
    setFormOpen(false)
    setFilterOpen(false)
    setSuggestionOpen(false)
  }, [])

  const useSuggestion = useCallback((e: GenericObjectType) => {
    setSuggestion({...e})
    setFilterOpen(false)
    setFormOpen(true)
  }, [])

  useEffect(() => {
    getSchedules()
  }, [getSchedules])


    useEffect(() => { 
        changeCurrentPage("My Schedules")
    },[changeCurrentPage])
    
  return (
    <Card>
      <div className="w-full mt-3 -mb-2 flex items-center justify-between">
        <div className="w-full max-w-[150px] gap-5 flex items-center">
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
                <div className="absolute left-1 mt-5 flex items-center justify-center">
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
            styleClass=''
            delay={0.8}
            type="container"
          >
            <button 
              type="button" 
              className='cursor-pointer flex items-center gap-3 text-[16px] font-semibold py-[2.6px] rounded-lg px-4 text-white hover:text-bowen-blue transition-colors duration-500 ease-out border-1 border-transparent hover:border-bowen-blue hover:bg-transparent bg-bowen-blue'
              onClick={openFormModal}
            >
              Add <span className='text-[24px] font-bold'>+</span>
            </button>
          </ComponentReveal>
        </div>
        <ComponentReveal
          styleClass=''
          delay={0.8}
          type="container"
        >
          <button 
            type="button" 
            className='cursor-pointer flex items-center gap-3 text-[16px] font-semibold py-2 rounded-lg px-4 text-white hover:text-citrus-spice transition-colors duration-500 ease-out border-1 border-transparent hover:border-citrus-spice hover:bg-transparent bg-citrus-spice'
            onClick={openSuggestionModal}
          >
            Suggestions
          </button>
        </ComponentReveal>
      </div>

      <Table
        displayKeys={["courseCodesString", "classroomsString", "day", "time"]}
        emptyText='schedules'
        listStyle='grid-cols-[0.2fr_0.5fr_0.5fr_0.4fr_0.5fr_0.4fr]'
        headers={headers}
        list={schedules}
        loading={loading}
        isFilterable={true}
        canEdit={true}
        canDelete={true}
        tableHeight='h-[62.5vh]'
        deleteApiRoute={ApiRoutes.SCHEDULE.delete}
        selectEntity={selectSchedule}
        refetchMethod={getSchedules}
      />

      {filterOpen && 
        <UpsertModal isOpen={filterOpen} closeModal={closeModal}>
          <ScheduleFilter setFilter={setFilterParams} closeModal={closeModal}/>
        </UpsertModal>
      }

      {suggestionOpen &&
        <Modal isOpen={suggestionOpen} closeModal={closeModal}>
          <Suggestions saveSuggestion={useSuggestion} closeModal={closeModal}/>
        </Modal>
      }

      {formOpen && 
        <UpsertModal isOpen={formOpen} closeModal={closeModal}>
          <Form inputValues={inputValues} suggestion={suggestion} closeModal={closeModal} refetchMethod={getSchedules}/>
        </UpsertModal>
      }
    </Card>
  )
}

export default React.memo(Schedules)