import MenuItem from '@/components/common/menu/MenuItem'

const Menu = () => {
  return (
    <div className="w-16 h-screen gap-9 flex flex-col items-center px-4 py-3 border-r border-solidborder-amber-300">
       logo
       <div className="flex flex-col gap-6">
        <MenuItem label="홈" onClick={() => {}} />
        <MenuItem label="여행" onClick={() => {}} />
        <MenuItem label="분석" onClick={() => {}} />
       </div>
      
    </div>
  )
}

export default Menu
