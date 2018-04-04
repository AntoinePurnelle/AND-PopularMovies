/*
 * Parts of this class have been inspired by Google's Android Fragment Transitions: RecyclerView to ViewPager
 * available at https://github.com/google/android-transition-examples/tree/master/GridToPager
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ouftech.popularmovies;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import net.ouftech.popularmovies.commons.CollectionUtils;
import net.ouftech.popularmovies.details.DetailsFragmentBuilder;


public class ImagePagerAdapter extends FragmentStatePagerAdapter {

    private Fragment fragment;

    public ImagePagerAdapter(Fragment fragment) {
        // Note: Initialize with the child fragment manager.
        super(fragment.getChildFragmentManager());
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        if (fragment != null && fragment.getActivity() != null && fragment.getActivity() instanceof MainActivity)
            return CollectionUtils.getSize(((MainActivity) fragment.getActivity()).getMovies());

        return 0;
    }

    @Override
    public Fragment getItem(int position) {
        return DetailsFragmentBuilder.newDetailsFragment(position);
    }
}
