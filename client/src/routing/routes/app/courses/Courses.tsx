import React, { useEffect, useState, useCallback } from 'react'
import { useApi, useNav } from '../../../../hooks/useContexts'
import { GenericObjectType } from '../../../../utils/Types'
import { ComponentReveal } from '../../../../ui/animations/ComponentAnimations'
import { ListFilterIcon as Filter } from 'lucide-react'
import { motion } from 'framer-motion'
import { CourseFilter } from '../../../../ui/Filters'
import UpsertModal from '../UpsertModal'
import Form from './Form/Form'
import Card from '../Card'
import Table from '../../../../ui/table/Table'
import ApiRoutes from '../../../../utils/ApiRoutes'

const Courses:React.FC = () => {
  const headers:string[] = [
    'S/N',
    'COURSE CODE(S)',
    'EXPECTED CLASS SIZE',
    'UNIT',
    'ACTIONS'
  ]

  const { changeCurrentPage } = useNav();
  const { makeGetRequest } = useApi();
  
  const [filterParams, setFilterParams] = useState<GenericObjectType | undefined>({
    courseUnit: null,
    department: null,
    level: null,
    isShared: false,
    isBatched: false,
    isGeneral: false,
})
  const [inputValues, setInputValues] = useState<GenericObjectType | undefined>(undefined)
  const [courses, setCourses] = useState<GenericObjectType[]>([])
  
  const [filterOpen, setFilterOpen] = useState<boolean>(false)
  const [formOpen, setFormOpen] = useState<boolean>(false)
  const [filterView, setFilterView] = useState<boolean>(false)
  const [loading, setLoading] = useState<boolean>(false)

  const getCourses = useCallback(async () => {
    setLoading(true)
    
    const { success, responseBody } = await makeGetRequest(ApiRoutes.COURSE.get, filterParams);
    if(success){
      const updatedCourses = []
      for(let course of [...responseBody!.result]){
        const updatedCourse = {
          ...course,
          courseCodesString: course?.courseCodes?.join(", "),
          isGeneral: course?.general,
          isBatched: course?.batched,
          isShared: course?.shared
        }
        updatedCourses.push(updatedCourse)
      }
      setCourses([...updatedCourses])
    }
    setLoading(false)
  }, [filterParams])

  const selectCourse = useCallback((e:GenericObjectType) => {
    setInputValues({...e})
    setFormOpen(true)
  }, [])

  const openFormModal = useCallback(() => {
    setInputValues(undefined)
    setFormOpen(true)
  }, [])

  const openFilterModal = useCallback(() => {
    setFilterOpen(true)
  }, [])

  const closeModal = useCallback(() => {
    setFormOpen(false)
    setFilterOpen(false)
  }, [])

  useEffect(() => {
    getCourses()
  }, [getCourses])

  // FOR GENERAL COURSES, ADD DEPARTMENTS BY COLLEGES WHICH IS EASIER.

  useEffect(() => { 
      changeCurrentPage("Courses")
  },[changeCurrentPage])
    
  return (
    <Card>
      <div className="w-full max-w-[150px] gap-5 mt-3 -mb-2 flex items-center">
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

      <Table
        emptyText='courses'
        listStyle='grid-cols-[0.2fr_0.8fr_0.4fr_0.2fr_0.4fr]'
        displayKeys={["courseCodesString", "expectedClassSize", "courseUnit"]}
        headers={headers}
        list={courses}
        loading={loading}
        canEdit={true}
        canDelete={false}
        isFilterable={true}
        tableHeight='h-[62.5vh]'
        selectEntity={selectCourse}
      />

      {filterOpen && 
        <UpsertModal isOpen={filterOpen} closeModal={closeModal}>
          <CourseFilter setFilter={setFilterParams} closeModal={closeModal}/>
        </UpsertModal>
      }

      {formOpen && 
        <UpsertModal isOpen={formOpen} closeModal={closeModal}>
          <Form inputValues={inputValues} closeModal={closeModal} refetchMethod={getCourses}/>
        </UpsertModal>
      }
    </Card>
  )
}

export default React.memo(Courses);