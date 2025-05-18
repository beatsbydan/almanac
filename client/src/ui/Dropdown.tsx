import React, { 
    useState,
    useRef, 
    useEffect, 
    useMemo, 
    useCallback 
} from 'react'
import { DropDownType, RenderedDropdownDisplayType } from '../utils/Types'
import { 
    ChevronDown as ArrowDown, 
    ChevronUp as ArrowUp, 
    Search, 
    X 
} from 'lucide-react'
import { useApi } from '../hooks/useContexts'
import Scroll from './Scroll'
import Spinner from './Spinner'

const RenderedDisplay:React.FC<RenderedDropdownDisplayType> = React.memo(({options, isLoading, handleChange}) => {
    const isEmpty = useMemo(() => {
        return options?.length === 0 || !options
    }, [options])

    return (
        <ul className="w-full h-[200px] bg-white z-10 border-1 rounded-lg absolute mt-2">
            {isLoading ? 
                <Spinner spinnerStyleClass='w-6 aspect-square absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2'/>
                :
                ((!isLoading && isEmpty) || isEmpty) ?
                    <li className='text-center pt-6 text-sm top-1/2 -translate-y-1/2 relative'>No options available</li>
                :
                <Scroll>
                    {options?.map((value, index) => {
                    return (
                        <li className="p-2 hover:bg-dropdown-option transition-colors duration-500 ease-out rounded-t-lg text-[14px] text-darkGray cursor-pointer" key={index} onClick={()=>handleChange(value)}>{value}</li>
                    )
                    })}
                </Scroll>
            }
        </ul>
    )
})

const Dropdown:React.FC<DropDownType> = ({
    value,
    isListInput,
    placeHolder,
    options,
    optionsApiRoute,
    canSearch,
    searchPlaceholder,
    searchOnly,
    error,
    save,
    saveOptions
}) => {
    const [dropdownValue, setDropdownValue] = useState<string | undefined>(placeHolder);

    const [open, setOpen] = useState<boolean>(false);
    const [fetching, setFetching] = useState<boolean>(false);
    const [search, setSearch] = useState<boolean>(false);
    
    const ref = useRef<HTMLDivElement>(null);

    const [searchValue, setSearchValue] = useState<string | undefined>("")

    const isEmpty = useMemo(() => {
        return options?.length === 0 || !options
    }, [options])

    const isUnselected = useMemo(() => {
        return dropdownValue === placeHolder
    }, [dropdownValue, placeHolder])

    const { makeGetRequest } = useApi();

    const fetchOptions = useCallback( async () => {
        if(open && isEmpty && !searchOnly){
            setFetching(true)
            const { success, responseBody, error } = await makeGetRequest(optionsApiRoute!, {key: searchValue})
            if(success){
                saveOptions!([...responseBody!.result])
                setOpen(true)
            }
            else{
                console.log(error)
            }
            setFetching(false)
        }
    }, [open, isEmpty, optionsApiRoute, makeGetRequest])

    useEffect(() => {
        fetchOptions()
    }, [fetchOptions])

    useEffect(() => {
        const timeout = setTimeout(async () => {
            if((searchOnly || canSearch) && searchValue !== "" ) {
                setOpen(true)
                setFetching(true)
                const route = `${optionsApiRoute!}/${`${searchValue}`}`
                const { responseBody } = await makeGetRequest(route)
                saveOptions!([...responseBody!.result])
                setFetching(false)
            }
        }, 500)

        return () => clearTimeout(timeout);
    }, [searchValue])

    const handleSearchValueChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSearchValue(e.target.value);
    }

    const handleChange = (e: string) => {
        setOpen(!open);
        setSearch(false)
        save!(e);
        isListInput ? setDropdownValue(placeHolder) : setDropdownValue(e)
        setSearchValue("")
    }

    useEffect(()=>{
        const closeDropDown = (e:MouseEvent) => {
            if(ref.current && !ref.current.contains(e.target as Node)){
                setOpen(false)
            }
        }
        document.addEventListener('click', closeDropDown)

        return () => {
            document.removeEventListener('click', closeDropDown)
        }
    },[])

    useEffect(() => {
        if(value){
            setDropdownValue(value)
        }
    }, [value, options])

    return (
        <div className="w-full relative mt-5 sm:mt-0">
            <div 
                className={`${error?.valid ? "border-red-600" : "border-light-black"} flex rounded-lg border-[0.1rem] border-light-black p-3 justify-between w-full items-center cursor-pointer`} 
                ref={ref}
            >
                {search || (searchOnly && isUnselected) ?
                    <input
                        type="text"
                        value={searchValue}
                        onChange={handleSearchValueChange}
                        placeholder={searchPlaceholder}
                        className="w-full text-[13px] py-1"
                    />
                    :
                    <span className="text-[14px] font-bold w-full cursor-pointer" onClick={()=>setOpen(!open)}>{dropdownValue}</span>
                }
                <div className='flex items-center gap-2'>
                    {!searchOnly && <div className="" onClick={()=>setOpen(!open)}>
                        {open ? 
                            <ArrowUp/> 
                            : 
                            <ArrowDown/>
                        }
                    </div>}
                    {(canSearch && search) ? 
                        <X cursor={"pointer"} size={19} onClick={()=>setSearch(false)}/> 
                        : 
                        canSearch ?
                            <Search size={18} cursor={"pointer"} onClick={()=>setSearch(true)}/>
                        :
                        null
                    }
                </div>
            </div>
            {open && 
                <RenderedDisplay
                    isLoading={fetching}
                    handleChange={handleChange}
                    options={options}
                />
            }
            
        </div>
    )
}

export default React.memo(Dropdown)