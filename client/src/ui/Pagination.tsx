import React, { useState, useEffect, useMemo } from 'react'
import { PaginationType } from '../utils/Types';
import Scroll from './Scroll';

const Pagination:React.FC<PaginationType> = ({details, method}) => {
    let { currentPage, totalPages } = details;

    const [pages, setPages] = useState<number[]>([])
    const [page, setPage] = useState<number>(currentPage)

    const isCurrentPage = useMemo(() => {
        return page === currentPage
    }, [page, currentPage])

    useEffect(() => {
        setPage(currentPage)
    }, [currentPage])

    useEffect(() => {
        const newPagesList:number[] = [];

        for(let i = 1; i <= totalPages; i++){
            newPagesList.push(i);
        }

        setPages([...newPagesList])
    },[totalPages])

    const getFirstPage = () => {
        if(totalPages !== 1) method(0)
    }

    const getLastPage = () => {
        if(totalPages !== 1) method(totalPages - 1)
    }

    const getNextPage = () => {
        if(currentPage < totalPages - 1)method(currentPage + 1)
    }

    const getPrevPage = () => {
        if(currentPage > 0) method(currentPage - 1)
    }

    const getCurrPage = (value: number) => {
        method(value - 1)
    }

    return (
        <div className="w-full max-w-[350px] mx-auto flex items-center justify-center mt-2 gap-3 text-[12px]">
            <button type="button" onClick={getFirstPage} className="flex justify-center items-center w-8 h-8 p-2 border-black rounded-md border-[1px] cursor-pointer hover:bg-gray duration-300 ease-in-out">
                {`<<`}
            </button>
            <button type="button" onClick={getPrevPage} className="flex justify-center items-center w-8 h-8 p-2 border-black rounded-md border-[1px] cursor-pointer hover:bg-gray duration-300 ease-in-out">
                {`<`}
            </button>
            <div className='w-auto'>
                <Scroll>
                    <ul className="w-full max-w-[220px] flex items-center gap-2 py-3">
                        {pages.map((value, index) => {
                            return(
                                <li key={index}>
                                    <button type="button" onClick={()=>getCurrPage(value)} className={`w-8 h-8 p-2 flex justify-center items-center border-black rounded-md border-[1px] cursor-pointer duration-300 ease-in-out ${ isCurrentPage ? "bg-black text-white hover:bg-black" : "bg-transparent hover:bg-gray hover:text-white"}`}>{value}</button>
                                </li>
                            )
                        })}
                    </ul>
                </Scroll>
            </div>
            <button type="button" onClick={getNextPage} className=" flex justify-center items-center w-8 h-8 p-2 border-black rounded-md border-[1px] cursor-pointer hover:bg-gray duration-300 ease-in-out">
                {`>`}
            </button>
            <button type="button" onClick={getLastPage} className="flex justify-center items-center w-8 h-8 p-2 border-black rounded-md border-[1px] cursor-pointer hover:bg-gray duration-300 ease-in-out">
                {`>>`}
            </button>
        </div>
    )
}

export default Pagination