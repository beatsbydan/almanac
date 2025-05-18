import React, { useState, useCallback, useEffect } from 'react'
import { GenericObjectType } from '../../../../utils/Types';
import { useNav, useApi } from '../../../../hooks/useContexts';
import ApiRoutes from '../../../../utils/ApiRoutes';
import { ComponentReveal } from '../../../../ui/animations/ComponentAnimations';
import Card from '../Card';
import Table from '../../../../ui/table/Table';
import UpsertModal from '../UpsertModal';
import Form from './Form';

const Colleges:React.FC = () => {
  const headers:string[] = [
    'S/N',
    'NAME',
    'SHORT NAME',
    'ACTIONS'
  ]
  const { changeCurrentPage } = useNav();
  const { makeGetRequest } = useApi();
  
  const [open, setOpen] = useState<boolean>(false)
  const [loading, setLoading] = useState<boolean>(false)
  const [inputValues, setInputValues] = useState<GenericObjectType | undefined>(undefined)
  const [colleges, setColleges] = useState<GenericObjectType[]>([])

  const getColleges = useCallback(async () => {
    setLoading(true)
    const { success, responseBody } = await makeGetRequest(ApiRoutes.COLLEGE.get);
    if(success){
      setColleges([...responseBody!.result])
    }
    setLoading(false)
  }, [])

  const selectCollege = useCallback((e:GenericObjectType) => {
    setInputValues({...e})
    setOpen(true)
  }, [])

  const openModal = useCallback(() => {
    setInputValues(undefined)
    setOpen(true)
  }, [])

  const closeModal = useCallback(() => {
    setOpen(false)
  }, [])

  useEffect(() => {
    getColleges()
  }, [getColleges])

  useEffect(() => { 
      changeCurrentPage("Colleges")
  },[changeCurrentPage])
    
  return (
    <Card>
      <ComponentReveal
        styleClass='mt-3 -mb-2 flex justify-end w-full'
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
      
      <Table
        emptyText='colleges'
        displayKeys={["collegeName", "collegeShortName"]}
        listStyle='grid-cols-[0.3fr_1.8fr_1fr_1fr]'
        headers={headers}
        list={colleges}
        loading={loading}
        isFilterable={false}
        canEdit={true}
        canDelete={false}
        tableHeight='h-[62.5vh]'
        selectEntity={selectCollege}
      />
      
      {open && 
        <UpsertModal isOpen={open} closeModal={closeModal}>
          <Form inputValues={inputValues} closeModal={closeModal} reFetchMethod={getColleges}/>
        </UpsertModal>
      }
    </Card>
  )
}

export default Colleges