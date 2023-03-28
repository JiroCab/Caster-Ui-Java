package casterui.utill;

public class UnitCounter {
   /*TODO: Find if separating the unit counting from the Ui is needed or useful
    public Iterator<Unit> unitsIterator

    public Map top = new HashMap<>();
    
    public UnitCounter(boolean showCoreUnits){
        final int[] unitInt = {0};
        Seq<Unit> unitsSorted = new Seq<>();
        unitsSorted.clear();
        Groups.unit.copy(unitsSorted);
        unitsSorted.sort(Structs.comps(Structs.comparing(Unit::team), Structs.comparing(Unit::type)));

        Unit prevUnit = null;
        final int[] unitCount = {0};
        unitsSorted.forEach((unit) -> {

            if (unitTableCoreUnits && unit.spawnedByCore) return;
            unitInt[0]++;
            if (prevUnit != unit) {
                table.image(unit.icon()).tooltip(unit.team.localized());
                table.labelWrap("["+unit.team.color.toString()+"]"+ unitCount[0] +"[]") ;
                if(unitInt[0] >= 6){ table.row(); }

            } else {
                unitCount[0]++;
            }
            prevUnit = unit;
        });
    }

    */
}
