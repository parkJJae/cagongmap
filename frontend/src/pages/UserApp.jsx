import { useState } from "react";
import Home from "./Home";
import MapView from "./MapView";
import NewCafe from "./NewCafe";

export default function UserApp() {
    const [view, setView] = useState("home"); // home | map | new

    const renderView = () => {
        switch (view) {
            case "map":
                return <MapView onBack={() => setView("home")} />;
            case "new":
                return <NewCafe onBack={() => setView("home")} />;
            default:
                return (
                    <Home
                        onGotoMap={() => setView("map")}
                        onGotoNew={() => setView("new")}
                    />
                );
        }
    };

    return <div className="app-shell">{renderView()}</div>;
}