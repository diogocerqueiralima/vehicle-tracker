"use client"

import React, {useState} from "react";
import {IoLogOut} from "react-icons/io5";
import Image from "next/image";

interface Item {

    name: string
    icon: React.ReactNode

}

interface Props {
    items: Item[]
}

export default function Header( { items } : Props ) {

    const [isOpen, setIsOpen] = useState(false)

    return (
        <header className={`
            flex flex-col justify-between items-center bg-surface
            text-foreground h-screen py-10 px-4 transition-all duration-200
        `}>

            <div className="flex flex-col gap-12 items-center">

                { /* 1. Logo that open or close the sidebar */ }

                <Image
                    src="/icon.png"
                    alt="MyTracker Logo"
                    width={80}
                    height={80}
                    className="rounded-full cursor-pointer duration-200 hover:scale-105"
                    onClick={() => setIsOpen(!isOpen)}
                />

                { /* 2. Navigate items */ }

                <ul className="flex flex-col gap-4">

                    {

                        items.map((item, index) => (

                            <li key={index}>
                                <button
                                    className={`
                                        flex items-center bg-background text-foreground px-8 py-2
                                        shadow-sm cursor-pointer rounded-lg hover:bg-highlight
                                        duration-200 hover:scale-105 w-full ${isOpen ? "gap-2" : "gap-0"}
                                    `}
                                    type="button"
                                >
                                    <span>{item.icon}</span>

                                    <div
                                        className="grid transition-[grid-template-columns] duration-200 ease-in-out"
                                        style={{
                                            gridTemplateColumns: isOpen ? "1fr" : "0fr",
                                        }}
                                    >

                                      <span className="overflow-hidden whitespace-nowrap text-xl">
                                        {item.name}
                                      </span>

                                    </div>
                                </button>
                            </li>

                        ))

                    }

                </ul>

            </div>

            { /* 3. Logout button, that should have a component it self */ }

            <button
                className="bg-background text-foreground rounded-full p-4 flex items-center justify-center shadow-md hover:bg-highlight hover:scale-105 duration-200 cursor-pointer"
                aria-label="Logout"
                type="button"
            >
                <IoLogOut size={32} />
            </button>

        </header>
    )

}