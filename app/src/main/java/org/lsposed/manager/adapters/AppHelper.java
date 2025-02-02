/*
 * This file is part of LSPosed.
 *
 * LSPosed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSPosed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSPosed.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 EdXposed Contributors
 * Copyright (C) 2021 LSPosed Contributors
 */

package org.lsposed.manager.adapters;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.view.MenuItem;

import org.lsposed.manager.ConfigManager;
import org.lsposed.manager.R;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppHelper {

    public static final String SETTINGS_CATEGORY = "de.robv.android.xposed.category.MODULE_SETTINGS";
    private static List<PackageInfo> appList;

    public static Intent getSettingsIntent(String packageName, int userId, PackageManager packageManager) {
        Intent intent = getIntentForCategory(packageName, userId, packageManager, SETTINGS_CATEGORY);
        if (intent != null) {
            return intent;
        }
        return getIntentForCategory(packageName, userId, packageManager, Intent.CATEGORY_LAUNCHER);
    }

    public static void startActivityAsUser(Activity activity, Intent intent, UserHandle user) {
        try {
            //noinspection JavaReflectionMemberAccess
            var startActivityAsUserMethod = Activity.class.getMethod("startActivityAsUser", Intent.class, UserHandle.class);
            startActivityAsUserMethod.invoke(activity, intent, user);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static Intent getIntentForCategory(String packageName, int userId, PackageManager packageManager, String category) {
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(category);
        intentToResolve.setPackage(packageName);
        try {
            //noinspection JavaReflectionMemberAccess
            Method queryIntentActivitiesAsUserMethod = PackageManager.class.getMethod("queryIntentActivitiesAsUser", Intent.class, int.class, int.class);
            //noinspection unchecked
            List<ResolveInfo> ris = (List<ResolveInfo>) queryIntentActivitiesAsUserMethod.invoke(packageManager, intentToResolve, 0, userId);

            if (ris == null || ris.size() <= 0) {
                return null;
            }

            Intent intent = new Intent(intentToResolve);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);
            return intent;
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean onOptionsItemSelected(MenuItem item, SharedPreferences preferences) {
        int itemId = item.getItemId();
        if (itemId == R.id.item_sort_by_name) {
            item.setChecked(true);
            preferences.edit().putInt("list_sort", 0).apply();
        } else if (itemId == R.id.item_sort_by_name_reverse) {
            item.setChecked(true);
            preferences.edit().putInt("list_sort", 1).apply();
        } else if (itemId == R.id.item_sort_by_package_name) {
            item.setChecked(true);
            preferences.edit().putInt("list_sort", 2).apply();
        } else if (itemId == R.id.item_sort_by_package_name_reverse) {
            item.setChecked(true);
            preferences.edit().putInt("list_sort", 3).apply();
        } else if (itemId == R.id.item_sort_by_install_time) {
            item.setChecked(true);
            preferences.edit().putInt("list_sort", 4).apply();
        } else if (itemId == R.id.item_sort_by_install_time_reverse) {
            item.setChecked(true);
            preferences.edit().putInt("list_sort", 5).apply();
        } else if (itemId == R.id.item_sort_by_update_time) {
            item.setChecked(true);
            preferences.edit().putInt("list_sort", 6).apply();
        } else if (itemId == R.id.item_sort_by_update_time_reverse) {
            item.setChecked(true);
            preferences.edit().putInt("list_sort", 7).apply();
        } else {
            return false;
        }
        return true;
    }

    public static Comparator<PackageInfo> getAppListComparator(int sort, PackageManager pm) {
        ApplicationInfo.DisplayNameComparator displayNameComparator = new ApplicationInfo.DisplayNameComparator(pm);
        switch (sort) {
            case 7:
                return Collections.reverseOrder(Comparator.comparingLong((PackageInfo a) -> a.lastUpdateTime));
            case 6:
                return Comparator.comparingLong((PackageInfo a) -> a.lastUpdateTime);
            case 5:
                return Collections.reverseOrder(Comparator.comparingLong((PackageInfo a) -> a.firstInstallTime));
            case 4:
                return Comparator.comparingLong((PackageInfo a) -> a.firstInstallTime);
            case 3:
                return Collections.reverseOrder(Comparator.comparing(a -> a.packageName));
            case 2:
                return Comparator.comparing(a -> a.packageName);
            case 1:
                return Collections.reverseOrder((PackageInfo a, PackageInfo b) -> displayNameComparator.compare(a.applicationInfo, b.applicationInfo));
            case 0:
            default:
                return (PackageInfo a, PackageInfo b) -> displayNameComparator.compare(a.applicationInfo, b.applicationInfo);
        }
    }

    public static List<PackageInfo> getAppList(boolean force) {
        if (appList == null || force) {
            appList = ConfigManager.getInstalledPackagesFromAllUsers(PackageManager.GET_META_DATA | PackageManager.MATCH_UNINSTALLED_PACKAGES, true);
        }
        return appList;
    }
}
