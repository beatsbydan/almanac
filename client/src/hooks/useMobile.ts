import { useEffect, useState } from 'react'

const useMobile = (breakpoint:number = 1024) => {
    const [isMobile, setMobile] = useState<boolean>(window.innerWidth < breakpoint);
    useEffect(() => {
        const handleResize = () => setMobile(window.innerWidth < breakpoint);

        window.addEventListener("resize", handleResize)

        return ()=> window.removeEventListener("resize", handleResize);
    },[breakpoint])

    return isMobile;
}

export default useMobile