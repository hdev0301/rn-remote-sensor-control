/**
 * Copyright 2015-present Telldus Technologies AB.
 *
 * This file is part of the Telldus Live! app.
 *
 * Telldus Live! app is free : you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Telldus Live! app is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Telldus Live! app.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @providesModule Actions_AppState
 */

// @flow

'use strict';

import type { Action, ThunkAction } from './Types';

import { AppState } from 'react-native';

module.exports = {
	appStart: (): Action => ({
		type: 'APP_START',
	}),
	appState: (): ThunkAction => dispatch => {
		AppState.addEventListener('change', appState => {
			if (appState === 'active') {
				return dispatch({
					type: 'APP_FOREGROUND',
				});
			}
			if (appState === 'background') {
				return dispatch({
					type: 'APP_BACKGROUND',
				});
			}
		});
	},
};