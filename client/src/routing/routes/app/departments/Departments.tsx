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

const Departments:React.FC = () => {
  const headers:string[] = [
    'S/N',
    'NAME',
    'SHORT NAME',
    'COLLEGE',
    'ACTIONS'
  ]
  const { changeCurrentPage } = useNav();
  const { makeGetRequest } = useApi();
  
  const [filter, setFilter] = useState<string>("")
  const [open, setOpen] = useState<boolean>(false)
  const [loading, setLoading] = useState<boolean>(false)
  const [inputValues, setInputValues] = useState<GenericObjectType | undefined>(undefined)
  const [departments, setDepartments] = useState<GenericObjectType[]>([])

  const getDepartments = useCallback(async () => {
    setLoading(true)
    const params = {
      key: filter
    }
    const { success, responseBody } = await makeGetRequest(ApiRoutes.DEPARTMENT.get, params);
    if(success){
      setDepartments([...responseBody!.result])
    }
    setLoading(false)
  }, [filter])

  const selectDepartment = useCallback((e:GenericObjectType) => {
    setInputValues({...e})
    setOpen(true)
  }, [])

  const changeCollegeFilter = useCallback((value: string) => {
    setFilter(value)
  }, [])

  const openModal = useCallback(() => {
    setInputValues(undefined)
    setOpen(true)
  }, [])

  const closeModal = useCallback(() => {
    setOpen(false)
  }, [])

  useEffect(() => {
    getDepartments()
  }, [getDepartments])

  useEffect(() => { 
      changeCurrentPage("Departments")
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
        emptyText='departments'
        listStyle='grid-cols-[0.3fr_0.8fr_0.8fr_0.6fr_0.4fr]'
        displayKeys={["departmentName", "departmentShortName", "college"]}
        headers={headers}
        list={departments}
        loading={loading}
        isFilterable={true}
        canEdit={true}
        canDelete={false}
        selectEntity={selectDepartment}
      />
      
      {open && 
        <UpsertModal isOpen={open} closeModal={closeModal}>
          <Form inputValues={inputValues} closeModal={closeModal} refetchMethod={getDepartments}/>
        </UpsertModal>
      }
    </Card>
  )
}

export default React.memo(Departments)