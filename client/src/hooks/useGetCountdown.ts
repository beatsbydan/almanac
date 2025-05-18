import { useState, useCallback } from 'react'
import { GenericObjectType } from '../utils/Types';

const useGetCountdown = () => {
    const [countDownInfo, setCountDownInfo] = useState<GenericObjectType>({
        days: "",
        hours: "",
        minutes: ""
    });

    const addZeroToCountdownValue = (value: number) => {
        if(value < 10) return "0" + value.toString()
        else return value.toString()
    }

    const getCountDownInfo = useCallback((nextDate: string | Date | number) => {
        const currentWindowDate = new Date().getTime();
        const nextWindowDate = new Date(nextDate).getTime();;
        
        const difference = nextWindowDate - currentWindowDate;
        
        let currentCountDownInfo = {
            days: "",
            hours: "",
            minutes: ""
        }

        if(difference > 0){
            currentCountDownInfo.days = addZeroToCountdownValue(Math.floor(difference / (1000 * 60 * 60 * 24)));
            currentCountDownInfo.hours = addZeroToCountdownValue(Math.floor((difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)));
            currentCountDownInfo.minutes = addZeroToCountdownValue(Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60)));
        }
        else{
            currentCountDownInfo.days = "00"
            currentCountDownInfo.hours = "00"
            currentCountDownInfo.minutes = "00"
        }

        setCountDownInfo({...currentCountDownInfo})
    } ,[])

    return {
        countDownInfo,
        getCountDownInfo
    }
}

export default useGetCountdown