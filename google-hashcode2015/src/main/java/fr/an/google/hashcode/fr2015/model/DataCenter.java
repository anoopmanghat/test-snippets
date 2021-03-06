package fr.an.google.hashcode.fr2015.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import fr.an.google.hashcode.fr2015.model.DataCenterBuilder.UnavailableSlotInfo;
import fr.an.google.hashcode.fr2015.model.occ.ServerOcc;

public final class DataCenter {

    private final int rowsCounts;
	private final int rowSize;
	private final int groupsCount;
	private final int serversCount;
	
	
	private final Group[] groups;
	private final Row[] rows;
	
	private TreeMap<ServerType,RemainingServerTypeCountPerServerType> countPerServerTypes = 
	        new TreeMap<ServerType,RemainingServerTypeCountPerServerType>(ServerType.BY_EFF_COMP);
	
	private final ServerType[] serverTypes; // = countPerServerTypes.keySet().toArray
    private final RemainingServerTypeCountPerServerType[] countPerServerTypesArray; // = countPerServerTypes.values().toArray
	
	// useless in solving, only to print solution
	private final ServerOcc[] serverOccs;

	
	// ------------------------------------------------------------------------

    public DataCenter(int rowsCount, int rowSize, int groupsCount, int serversCount,
            List<DataCenterBuilder.UnavailableSlotInfo> unavailableSlotInfos,
            List<DataCenterBuilder.ServerInfo> serversDatas
            ) {
        this.rowsCounts = rowsCount;
        this.rowSize = rowSize;
        this.groupsCount = groupsCount;
        this.serversCount = serversCount;
        
        Map<Integer,Set<Integer>> unavailableSlotIdsMap = new HashMap<Integer,Set<Integer>>();
        for(UnavailableSlotInfo unavailable : unavailableSlotInfos) {
            int rowId = unavailable.rowId;
            int slotId = unavailable.slotId;
            Set<Integer> slotIdsPerRow = unavailableSlotIdsMap.get(rowId);
            if (slotIdsPerRow == null) {
                slotIdsPerRow = new HashSet<Integer>();
                unavailableSlotIdsMap.put(rowId, slotIdsPerRow);
            }
            slotIdsPerRow.add(slotId);
        }
        
        this.rows = new Row[rowsCounts];
        for (int i = 0; i < rowsCounts; i++) {
            Set<Integer> unavailableSlotIds = unavailableSlotIdsMap.get(i);
            rows[i] = new Row(i, rowSize, unavailableSlotIds);
        }
        this.groups = new Group[groupsCount];
        for (int i = 0; i < groupsCount; i++) {
            groups[i] = new Group(i);
        }
        
        this.serverOccs = new ServerOcc[serversCount];
        for (int i = 0; i < serversCount; i++) {
            int capacity = serversDatas.get(i).capacity;
            int size = serversDatas.get(i).size;

            ServerType tmpKey = new ServerType(capacity, size);
            RemainingServerTypeCountPerServerType sl = getOrCreateCountPerServerType(tmpKey);
            ServerType st = sl.serverType;
            serverOccs[i] = new ServerOcc(i, st);
            sl.initAddServerOcc(serverOccs[i]);
        }
        
        this.serverTypes = countPerServerTypes.keySet().toArray(new ServerType[countPerServerTypes.size()]);
        this.countPerServerTypesArray = countPerServerTypes.values().toArray(new RemainingServerTypeCountPerServerType[countPerServerTypes.size()]);
    }

    // ------------------------------------------------------------------------

    // for test only
    /*pp*/ RemainingServerTypeCountPerServerType getOrCreateCountPerServerType(int capacity, int size) { 
        return getOrCreateCountPerServerType(new ServerType(capacity, size));
    }
    
    public RemainingServerTypeCountPerServerType getOrCreateCountPerServerType(ServerType st) { 
        RemainingServerTypeCountPerServerType res = countPerServerTypes.get(st);
        if (res == null) {
            res = new RemainingServerTypeCountPerServerType(st);
            countPerServerTypes.put(st, res);
        }
        return res;
    }

    /** @return memento to pass to undo assign */
    public void addAssignIncr(ServerType st, int stCount, Group group, Row row, int spaceSize) {
        assignIncr(st, stCount, group, row, spaceSize);
        // return memento
    }
    
    public void removeAssignIncr(ServerType st, int stCount, Group group, Row row, int spaceSize
            // , Object undoMemento
            ) {
        assignIncr(st, -stCount, group, row, spaceSize);
    }
    
    protected void assignIncr(ServerType st, int stCount, Group group, Row row, int spaceSize) {
        RemainingServerTypeCountPerServerType countPerSt = getOrCreateCountPerServerType(st);
        countPerSt.assign(stCount);
        group.incrAssign(row, st, stCount);
        row.assignIncrServerTypeToSpaceType(spaceSize, st, stCount);
    }
    
    // ------------------------------------------------------------------------
    
	public TreeMap<ServerType, RemainingServerTypeCountPerServerType> getCountPerServerTypes() {
        return countPerServerTypes;
    }

    public int getRowsCount() {
		return rowsCounts;
	}
	
	public int getRowsCounts() {
        return rowsCounts;
    }

    public int getRowSize() {
        return rowSize;
    }

	public int getGroupsCount() {
		return groupsCount;
	}

	public int getServersCount() {
		return serversCount;
	}
	
	
	
	public ServerType[] getServerTypes() {
        return serverTypes;
    }
	
    public RemainingServerTypeCountPerServerType[] getCountPerServerTypesArray() {
        return countPerServerTypesArray;
    }

    public Group[] getGroups() {
		return groups;
	}
	
	public Row[] getRows() {
		return rows;
	}

	public ServerOcc[] getServerOccs() {
		return serverOccs;
	}
	
	
	public Row getRow(int index) {
	    return rows[index];
	}
	
}
