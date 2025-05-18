import React, { useState, useCallback, useEffect } from 'react'
import { useApi, useNav } from '../../../../hooks/useContexts';
import { CollegeFilter } from '../../../../ui/Filters';
import { GenericObjectType } from '../../../../utils/Types';
import { ComponentReveal } from '../../../../ui/animations/ComponentAnimations';
import Card from '../Card'
import UpsertModal from '../UpsertModal';
import Form from './Form';
import Table from '../../../../ui/table/Table';
import ApiRoutes from '../../../../utils/ApiRoutes';

const ClassRooms:React.FC = () => {
  const headers:string[] = [
    'S/N',
    'NAME',
    'COLLEGE',
    'CAPACITY',
    'ACTIONS'
  ]
  const { changeCurrentPage } = useNav();
  const { makeGetRequest } = useApi();
  
  const [filter, setFilter] = useState<string | null>(null)
  const [open, setOpen] = useState<boolean>(false)
  const [loading, setLoading] = useState<boolean>(false)
  const [inputValues, setInputValues] = useState<GenericObjectType | undefined>(undefined)
  const [classrooms, setClassrooms] = useState<GenericObjectType[]>([])

  const getClassRooms = useCallback(async () => {
    setLoading(true)
    const params = {
      college: filter
    }
    const { success, responseBody } = await makeGetRequest(ApiRoutes.CLASS_ROOM.get, params);
    if(success){
      setClassrooms([...responseBody!.result])
    }
    setLoading(false)
  }, [filter])

  const selectClassroom = useCallback((e:GenericObjectType) => {
    setInputValues({...e})
    setOpen(true)
  }, [])

  const changeCollegeFilter = useCallback((value: string) => {
    setFilter(value === "NONE" ? null : value)
  }, [])

  const openModal = useCallback(() => {
    setInputValues(undefined)
    setOpen(true)
  }, [])

  const closeModal = useCallback(() => {
    setOpen(false)
  }, [])

  useEffect(() => {
    getClassRooms()
  }, [getClassRooms])

  useEffect(() => { 
      changeCurrentPage("Classrooms")
  },[changeCurrentPage])
    
  return (
    <Card>
      <div className="w-full max-w-[500px] gap-5 mt-3 -mb-2 justify-between mx-auto flex items-center">
        <ComponentReveal
          styleClass='w-full'
          delay={0.6}
          type="container"
        >
          <CollegeFilter setFilter={changeCollegeFilter}/>
        </ComponentReveal>
        <ComponentReveal
          styleClass=''
          delay={0.8}
          type="container"
        >
          <button 
            type="button" 
            className='cursor-pointer flex items-center gap-3 text-[16px] font-semibold py-[2.6px] rounded-lg px-4 text-white hover:text-bowen-blue transition-colors duration-500 ease-out border-1 border-transparent hover:border-bowen-blue hover:bg-transparent bg-bowen-blue'
            onClick={openModal}
          >
            Add <span className='text-[24px] font-bold'>+</span>
          </button>
        </ComponentReveal>
      </div>
      
      <Table
        emptyText='classrooms'
        listStyle='grid-cols-[0.3fr_0.8fr_0.8fr_0.4fr_0.8fr]'
        displayKeys={["className", "college", "classSize"]}
        headers={headers}
        list={classrooms}
        loading={loading}
        isFilterable={true}
        canEdit={true}
        canDelete={false}
        selectEntity={selectClassroom}
      />
      
      {open && 
        <UpsertModal isOpen={open} closeModal={closeModal}>
          <Form inputValues={inputValues} closeModal={closeModal} refetchMethod={getClassRooms}/>
        </UpsertModal>
      }
    </Card>
  )
}

export default React.memo(ClassRooms)