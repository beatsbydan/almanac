import { GenericObjectType } from "./Types";

export const constructQueryParams = (params: GenericObjectType) => {
    let paramString = "?"
    const paramKeys = Object.keys(params);
    
    for(let i = 0; i < paramKeys.length; i++){
        paramString += `${paramKeys[i]}=${params[paramKeys[i]]}${(i !== paramKeys.length-1) && "&"}`
    }

    return paramString;
}

export const getSession = () => {
    const date = new Date()

    const isInCurrentSession = date.getMonth() < 7;
    
    const currentYear = date.getFullYear();

    const sessionStart = isInCurrentSession ? currentYear - 1 : currentYear
    const sessionEnd = isInCurrentSession ? currentYear : currentYear + 1

    return `${sessionStart}/${sessionEnd}`
}

const toTitleCase = (string: string) => {
    const caps = string[0].toUpperCase();
    return string.replace(string[0], caps);
}

const getDatePrefix = (day: number) => {
    if (day % 100 >= 11 && day % 100 <= 13) {
        return `${day}th`;
    }

    switch (day % 10) {
        case 1:
            return `${day}st`;
        case 2:
            return `${day}nd`;
        case 3:
            return `${day}rd`;
        default:
            return `${day}th`;
    }

}

export const formatDate = (date: string) => {
    const newDate = new Date(date);
    
    const year = newDate.getFullYear();
    const day = getDatePrefix(newDate.getDate());
    const month = toTitleCase(newDate.toLocaleString('en-US', {month: "long"}));

    return `${day} of ${month}, ${year}.`

}

export const generateFileName = (isStudent:boolean, timetable: GenericObjectType, user: GenericObjectType) => {
    const fileNature = isStudent ? `${user?.department} ${user?.level}` : `${user?.id}`
    return `BUI_${timetable?.session}_${timetable?.semester}_SEMESTER_${fileNature}_timetable.docx`
}