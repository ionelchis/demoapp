package com.example.demoapp.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.demoapp.fragments.ChatsFragment;
import com.example.demoapp.fragments.FriendsFragment;
import com.example.demoapp.fragments.FriendshipRequestsFragment;
import com.example.demoapp.fragments.GroupsFragment;

public class TabsAccessorAdapter extends FragmentStatePagerAdapter {

    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ChatsFragment();
            case 1:
                return new FriendsFragment();
            case 2:
                return new FriendshipRequestsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Friends";
            case 2:
                return "Requests";
            default:
                return null;
        }
    }

    //    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
//        super(fm,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
//    }
//
//    @NonNull
//    @Override
//    public Fragment getItem(int position) {
//        switch (position) {
//            case 0:
//                return new ChatsFragment();
//            case 1:
//                return new GroupsFragment();
//            case 2:
//                return new FriendsFragment();
//            default:
//                return null;
//        }
//    }
//
//    @Override
//    public int getCount() {
//        return 0;
//    }
//
//    @Nullable
//    @Override
//    public CharSequence getPageTitle(int position) {
//        switch (position) {
//            case 0:
//                return "Chats";
//            case 1:
//                return "Groups";
//            case 2:
//                return "Friends";
//            default:
//                return null;
//        }
//    }
}
