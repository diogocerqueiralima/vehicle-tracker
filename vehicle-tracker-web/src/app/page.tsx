"use client"

import Card, {ChangeType, Indicator} from "@/components/Card";
import {MdOnDeviceTraining, MdOutlineWarning} from "react-icons/md";
import {FaBell} from "react-icons/fa";
import {useAuthentication} from "@/context/AuthenticationContext";

export default function Home() {

    const { login } = useAuthentication()

  return (
    <div className={`flex flex-row gap-8`}>

        <p onClick={() => login()} className={`text-2xl font-bold cursor-pointer`}>
            Login
        </p>
      
        <Card title={"Dispositivos ativos"} label={"123"} indicator={Indicator.ONLINE} icon={<MdOnDeviceTraining size={32} />} change={`+3 esta semana`} changeType={ChangeType.POSITIVE}></Card>
        <Card
            title="Dispositivos offline"
            label="6"
            indicator={Indicator.OFFLINE}
            icon={<MdOutlineWarning size={32} />}
            change="+1 desde ontem"
            changeType={ChangeType.NEGATIVE}
        />
        <Card
            title="Alertas ativos"
            label="4"
            indicator={Indicator.OFFLINE}
            icon={<FaBell size={28} />}
            change="+2 nas últimas 24h"
            changeType={ChangeType.NEGATIVE}
        />

    </div>
  );
}
