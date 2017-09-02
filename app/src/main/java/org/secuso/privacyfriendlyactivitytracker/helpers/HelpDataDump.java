/*
    Privacy Friendly Pedometer is licensed under the GPLv3.
    Copyright (C) 2017  Tobias Neidig

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.secuso.privacyfriendlyactivitytracker.helpers;

import android.content.Context;

import org.secuso.privacyfriendlyactivitytracker.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Class structure taken from tutorial at http://www.journaldev.com/9942/android-expandablelistview-example-tutorial
 * last access 27th October 2016
 */

public class HelpDataDump {

    private Context context;

    public HelpDataDump(Context context) {
        this.context = context;
    }

    public LinkedHashMap<String, List<String>> getDataGeneral() {
        LinkedHashMap<String, List<String>> expandableListDetail = new LinkedHashMap<>();

        List<String> general = new ArrayList<>();
        general.add(context.getResources().getString(R.string.help_intro));
        general.add(context.getResources().getString(R.string.help_functionality_step_counter_summary));
        expandableListDetail.put(context.getResources().getString(R.string.help_overview_heading), general);

        List<String> notifications = new ArrayList<>();
        notifications.add(context.getResources().getString(R.string.help_functionality_notification_summary));
        notifications.add(context.getResources().getString(R.string.help_functionality_notification_delete));
        expandableListDetail.put(context.getResources().getString(R.string.help_functionality_notification), notifications);

        List<String> motivationalert = new ArrayList<>();
        motivationalert.add(context.getResources().getString(R.string.help_functionality_motivation_alert_summary));
        expandableListDetail.put(context.getResources().getString(R.string.help_functionality_motivation_alert), motivationalert);

        List<String> walkingModes = new ArrayList<>();
        walkingModes.add(context.getResources().getString(R.string.help_walkingmodes_selection));
        expandableListDetail.put(context.getResources().getString(R.string.help_walkingmodes_heading), walkingModes);

        List<String> training = new ArrayList<>();
        training.add(context.getResources().getString(R.string.help_training_selection));
        expandableListDetail.put(context.getResources().getString(R.string.help_training_heading), training);

        List<String> distancemeasurement = new ArrayList<>();
        distancemeasurement.add(context.getResources().getString(R.string.help_distancemeasurement_selection));
        expandableListDetail.put(context.getResources().getString(R.string.help_distancemeasurement_heading), distancemeasurement);

        List<String> correctsteps = new ArrayList<>();
        correctsteps.add(context.getResources().getString(R.string.help_correctsteps_selection));
        expandableListDetail.put(context.getResources().getString(R.string.help_correctsteps_heading), correctsteps);

        List<String> permissions = new ArrayList<>();
        permissions.add(context.getResources().getString(R.string.help_permissions_selection));
        expandableListDetail.put(context.getResources().getString(R.string.help_privacy_heading), permissions);

        List<String> bootpermission = new ArrayList<>();
        bootpermission.add(context.getResources().getString(R.string.help_permission_boot_description));
        expandableListDetail.put(context.getResources().getString(R.string.help_permission_boot_heading), bootpermission);

        List<String> wakePermission = new ArrayList<>();
        wakePermission.add(context.getResources().getString(R.string.help_permission_wake_description));
        expandableListDetail.put(context.getResources().getString(R.string.help_permission_wake_heading), wakePermission);

        return expandableListDetail;
    }

}
