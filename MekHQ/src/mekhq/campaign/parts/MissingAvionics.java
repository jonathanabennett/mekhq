/*
 * MissingAvionics.java
 *
 * Copyright (c) 2009 Jay Lawson (jaylawson39 at yahoo.com). All rights reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.parts;

import java.util.StringJoiner;

import megamek.common.annotations.Nullable;
import org.w3c.dom.Node;

import megamek.common.Aero;
import megamek.common.CriticalSlot;
import megamek.common.Dropship;
import megamek.common.Entity;
import megamek.common.EquipmentType;
import megamek.common.Jumpship;
import megamek.common.LandAirMech;
import megamek.common.Mech;
import megamek.common.TechAdvancement;
import mekhq.campaign.Campaign;

/**
 * @author Jay Lawson (jaylawson39 at yahoo.com)
 */
public class MissingAvionics extends MissingPart {
    public MissingAvionics() {
        this(0, null);
    }

    public MissingAvionics(int tonnage, Campaign c) {
        super(0, c);
        this.name = "Avionics";
    }

    @Override
    public int getBaseTime() {
        if (campaign.getCampaignOptions().isUseAeroSystemHits()) {
            int time = 0;
            // Test of proposed errata for repair times
            if (null != unit && (unit.getEntity() instanceof Dropship || unit.getEntity() instanceof Jumpship)) {
                time = 2400;
            } else {
                time = 600;
            }
            return time;
        }
        return 4800;
    }

    @Override
    public int getDifficulty() {
        return 1;
    }

    @Override
    public @Nullable String checkFixable() {
        if ((unit != null) && (unit.getEntity() instanceof LandAirMech)) {
            // Avionics are installed in the Head and both Torsos,
            // make sure they're not missing.
            StringJoiner missingLocs = new StringJoiner(", ");
            for (Part part : unit.getParts()) {
                if (part instanceof MissingMekLocation) {
                    switch (part.getLocation()) {
                        case Mech.LOC_HEAD:
                        case Mech.LOC_LT:
                        case Mech.LOC_RT:
                            missingLocs.add(unit.getEntity().getLocationName(part.getLocation()));
                            break;
                        default:
                            break;
                    }
                }
            }

            return missingLocs.length() == 0
                    ? null
                    : "Cannot reinstall avionics when missing: " + missingLocs;
        }

        return null;
    }

    @Override
    public Part getNewPart() {
        return new Avionics(getUnitTonnage(), campaign);
    }

    @Override
    public boolean isAcceptableReplacement(Part part, boolean refit) {
        return part instanceof Avionics;
    }

    @Override
    public double getTonnage() {
        return 0;
    }

    @Override
    public int getTechRating() {
        // go with conventional fighter avionics
        return EquipmentType.RATING_B;
    }

    @Override
    public void updateConditionFromPart() {
        if (null != unit && unit.getEntity() instanceof Aero) {
            ((Aero) unit.getEntity()).setAvionicsHits(3);
        } else if (null != unit && unit.getEntity() instanceof LandAirMech) {
            unit.damageSystem(CriticalSlot.TYPE_SYSTEM, LandAirMech.LAM_AVIONICS, 3);
        }
    }

    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        //nothing to load
    }

    @Override
    public String getLocationName() {
        if (null != unit) {
            return unit.getEntity().getLocationName(unit.getEntity().getBodyLocation());
        }
        return null;
    }

    @Override
    public int getLocation() {
        if (null != unit) {
            return unit.getEntity().getBodyLocation();
        }
        return Entity.LOC_NONE;
    }

    @Override
    public TechAdvancement getTechAdvancement() {
        return TA_GENERIC;
    }
}
