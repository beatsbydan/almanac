import React, { useState, useEffect, useMemo } from 'react'
import { InputType, DateType, RadioInputType, ListInputType } from '../utils/Types'
import { TextInputComponentReveal } from './animations/InputAnimations'
import { motion } from 'framer-motion'
import { X } from 'lucide-react'
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css'
import Dropdown from './Dropdown'
import Button from './Button'

export const Input:React.FC<InputType> = React.memo(({
    type, 
    name, 
    placeholder, 
    label, 
    value, 
    error, 
    delay, 
    onChange
}) => {
    const DEFAULT_STYLE = "w-full text-[13px] p-3 border-light-black rounded-md border-[0.1rem]"
    const ERROR_STYLE = "border-red-600"

    return (
        <div className="w-full flex flex-col gap-1">
            <motion.div 
                className='w-full flex justify-between items-center'
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, ease: "easeOut", delay: delay! / 2 }}
            >
                <small className='text-[13px] text-black w-[30%] font-medium'>{label}</small>
                <small className="text-red-600 text-[13px] h-[16px] font-medium w-[70%] text-end float-right mb-1">{error?.message}</small>
            </motion.div>
            <TextInputComponentReveal
                type={type}
                name={name}
                placeholder={placeholder}
                value={value}
                onChange={onChange}
                styleClass={error?.valid ? ERROR_STYLE + " w-[full] " + DEFAULT_STYLE : DEFAULT_STYLE}
                delay={delay}
            />
        </div>
    )
})

export const DateInput:React.FC<DateType> = React.memo(({title, placeholder, value, handleChange, delay, error}) => {
    const [isOpen, setIsOpen] = useState<boolean>(false)
    const [startDate, setStartDate] = useState<Date | null>(null);
    const [dateString, setDateString] = useState<string | null>(null)

    useEffect(() => {
        if(startDate){
            const month = startDate?.getMonth() + 1;
            const date = startDate?.getDate();
            let dateString = date < 10 ? `0${date}` : date
            let monthString = month < 10 ? `0${month}` : month

            const value = `${startDate?.getFullYear()}-${monthString}-${dateString}`
            setDateString(value)
            handleChange(value);
        }
    }, [startDate])

    const forceClose = () => {
        setIsOpen(false)
    }

    return (
        <motion.div 
            className="w-full flex flex-col gap-1 relative"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, ease: "easeOut", delay: delay }}
        >
            <p className='text-[13px] font-medium mb-[0.5px]'>{title}</p>
            <div className={`${error?.valid ? "border-red-600" : "border-black"}flex items-center justify-center gap-2 px-2 py-3 border-[0.1rem] rounded-lg w-full`} onClick={()=>setIsOpen(true)}>
                <span className={(startDate || value) ? "text-black text-center text-[12px]" : "text-gray text-center text-[12px]"}>{value !== "" ? value : dateString ? dateString : placeholder}</span>
            </div>
            {isOpen && 
                <div className="absolute -translate-x-[calc(100%+30px)] -translate-y-1/2 top-1/2 z-20">
                <DatePicker
                    onClickOutside={forceClose}
                    onSelect={forceClose}
                    inline
                    selected={startDate} 
                    onChange={(date: Date | null) => setStartDate(date)}
                />
                </div>
            }
        </motion.div>
    )
})

export const RadioButtonInput:React.FC<RadioInputType> = ({label, value, handleChange, delay}) => {
    return (
        <motion.div 
            className='flex items-center gap-2'
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, ease: "easeOut", delay: delay }}
        >
            <div 
                className={`${value ? "border-bowen-blue" : "border-light-gray"} border-1 cursor-pointer rounded-full p-1`}
                onClick={handleChange}
            >
                <div className={`${value ? "bg-bowen-blue" : "bg-transparent"} w-3 aspect-square rounded-full`}/>
            </div>
            <span className={`${value ? "text-black" : "text-light-gray"} font-medium text-[13px]`}>{label}</span>
        </motion.div>
    )
}

export const ListInput:React.FC<ListInputType> = ({
    save, 
    saveOptions,
    label,
    delay,
    inputValuesList, 
    placeHolder, 
    emptyText, 
    searchPlaceHolder,
    optionsApiRoute,
    isDropdown,
    isSearch,
    canSearch,
    options,
    error
}) => {
    const [inputValue, setInputValue] = useState<string>("")
    const [list, setList] = useState<string []>([])

    const isEmpty = useMemo(() => {
        return list.length === 0
    }, [list])

    const addToList = () => {
        if(!list.includes(inputValue)){
            const newItems = [...list, inputValue]
            setList(newItems)
            save(newItems)
            setInputValue("")
        }
    }

    const addToDropdownList = (e: string) => {
        if(!list.includes(e)){
            const newItems = [...list, e]
            setList(newItems)
            save(newItems)
            setInputValue("")
        }
    }

    const removeFromList = (value: string) => {
        const remainingItems = list.filter(item => item !== value)
        setList(remainingItems)
        save(remainingItems)
    }

    useEffect(() => {
        if(inputValuesList!.length > 0) setList([...inputValuesList!])
    }, [inputValuesList])

    return (
        <div className='w-full relative'>
            {isDropdown ?
                <Dropdown
                    save={addToDropdownList}
                    saveOptions={saveOptions}
                    placeHolder={placeHolder}
                    options={options}
                    optionsApiRoute={optionsApiRoute}
                    value={undefined}
                    canSearch={canSearch}
                    isListInput={true}
                    searchPlaceholder={searchPlaceHolder}
                    searchOnly={isSearch}
                    error={error}
                /> 
                :
                <div className='w-full flex items-center -mt-[10px] gap-2'>
                    <div className='w-[75%]'>
                        <Input
                            type="text"
                            name="inputValue" 
                            placeholder={searchPlaceHolder}
                            label={label}
                            value={inputValue} 
                            delay={delay} 
                            error={error}
                            onChange={(e)=>setInputValue(e.target.value)}
                        />
                    </div>
                    <div className='w-[25%]'>
                        <Button
                            processing={false}
                            onClick={addToList}
                            styleClass='bg-bowen-blue hover:border-bowen-blue text-white font-semibold mt-6'
                            spinnerStyleClass='w-6 h-6 mx-auto'
                            delay={0.8}
                        >
                            Add
                        </Button>
                    </div>
                </div>
            }
            <ul className={`w-full flex flex-wrap items-center gap-x-2 gap-y-1 mt-2`}>
                {
                    isEmpty ? <li className=' w-full mx-auto text-center justify-center text-light-gray text-[13px] font-medium'>{`No ${emptyText} selected..`}</li>
                    :
                    list.map((value, index) => {
                        return (
                            <li className="flex items-center gap-1 p-1 rounded-sm bg-black" key={index}>
                                <span className="text-[10px] text-white font-medium">{value}</span>
                                <X size={15} className="text-light-gray cursor-pointer" onClick={()=>removeFromList(value)}/>
                            </li>
                        )
                    })
                }
            </ul>
        </div>
    )
}