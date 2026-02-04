import React from "react";
import {IoLogOut} from "react-icons/io5";

interface Item {

    name: string
    icon: React.ReactNode

}

interface Props {
    items: Item[]
}

export default function Header( { items } : Props ) {

    return (
        <header className={`flex flex-col justify-between items-center px-10 bg-surface w-fit h-screen py-10`}>

            { /* 1. Navigation items */ }

            <ul className={`flex flex-col gap-8`}>

                <li className={`flex flex-col gap-4`}>

                    {
                        items.map((item, index) => (
                            <div
                                key={index}
                                className={`
                                    flex
                                    flex-row
                                    items-center
                                    bg-background
                                    px-8
                                    py-2
                                    shadow-sm
                                    cursor-pointer
                                    rounded-lg
                                    gap-2
                                    hover:bg-highlight
                                    duration-200
                                `}
                            >
                                {item.icon}
                                <div className={`text-xl`}>
                                    {item.name}
                                </div>
                            </div>
                        ))
                    }

                </li>

            </ul>

            { /* 2. Logout section, it should be replaced with a component that will be rendered or not */ }

            <button
                className={`
                    bg-background
                    text-foreground
                    rounded-full
                    p-4
                    flex
                    items-center
                    justify-center
                    shadow-md
                    hover:bg-highlight
                    hover:scale-105
                    duration-200
                    cursor-pointer
                `}
                aria-label="Logout"
            >
                <IoLogOut size={32} />
            </button>

        </header>
    )

}