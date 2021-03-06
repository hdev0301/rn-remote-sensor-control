/**
 * Copyright 2016-present Telldus Technologies AB.
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
 */

// @flow

'use strict';

import React from 'react';
import { connect } from 'react-redux';

import { RoundedCornerShadowView, Text, View } from 'BaseComponents';
import { StyleSheet } from 'react-native';
import Slider from 'react-native-slider';
import { OnButton, OffButton, LearnButton } from 'TabViews_SubViews';

import { setDimmerValue, updateDimmerValue } from 'Actions_Dimmer';

type Props = {
  device: Object,
  onTurnOff: number => void,
  onTurnOn: number => void,
  onLearn: number => void,
  onDim: number => void,
};

type State = {
  temporaryDimmerValue: number,
};

const ToggleButton = ({ device }) => (
	<RoundedCornerShadowView style={styles.toggleContainer}>
		<OffButton id={device.id} isInState={device.isInState} fontSize={16} style={styles.turnOff} methodRequested={device.methodRequested} />
		<OnButton id={device.id} isInState={device.isInState} fontSize={16} style={styles.turnOn} methodRequested={device.methodRequested} />
	</RoundedCornerShadowView>
);

class DimmerDeviceDetailModal extends View {
	props: Props;
	state: State;
	currentDimmerValue: number;
	onTurnOn: () => void;
	onTurnOff: () => void;
	onLearn: () => void;
	onValueChange: number => void;
	onSlidingComplete: number => void;

	constructor(props: Props) {
		super(props);

		const dimmerValue: number = this.getDimmerValue(this.props.device);

		this.state = {
			temporaryDimmerValue: dimmerValue,
		};

		this.currentDimmerValue = dimmerValue;
		this.onValueChange = this.onValueChange.bind(this);
		this.onSlidingComplete = this.onSlidingComplete.bind(this);
	}

	getDimmerValue(device: Object) : number {
		if (device !== null && device.value !== null) {
			if (device.isInState === 'TURNON') {
				return 100;
			} else if (device.isInState === 'TURNOFF') {
				return 0;
			} else if (device.isInState === 'DIM') {
				return Math.round(device.value * 100.0 / 255);
			}
		}
		return 0;
	}

	onValueChange(value) {
		this.setState({ temporaryDimmerValue: value });
	}

	onSlidingComplete(value) {
		this.props.onDim(this.props.device.id, 255 * value / 100.0);
	}

	componentWillReceiveProps(nextProps) {
		const device = nextProps.device;
		const dimmerValue = this.getDimmerValue(device);
		if (this.currentDimmerValue !== dimmerValue) {
			this.setState({ temporaryDimmerValue: dimmerValue });
			this.currentDimmerValue = dimmerValue;
		}

		this.setState({ request: 'none' });
	}

	render() {
		const { device } = this.props;
		const { TURNON, TURNOFF, LEARN, DIM } = device.supportedMethods;

		let toggleButton = null;
		let learnButton = null;
		let slider = null;

		if (TURNON || TURNOFF) {
			toggleButton = <ToggleButton device={device} onTurnOn={this.onTurnOn} onTurnOff={this.onTurnOff}/>;
		}

		if (LEARN) {
			learnButton = <LearnButton id={device} style={styles.learn} />;
		}

		if (DIM) {
			slider = <Slider minimumValue={0} maximumValue={100} step={1} value={this.currentDimmerValue}
			                 style={{
				                 marginHorizontal: 8,
				                 marginVertical: 8,
			                 }}
			                 minimumTrackTintColor="rgba(0,150,136,255)"
			                 maximumTrackTintColor="rgba(219,219,219,255)"
			                 thumbTintColor="rgba(0,150,136,255)"
			                 onValueChange={this.onValueChange}
			                 onSlidingComplete={this.onSlidingComplete}
			                 animateTransitions={true}/>;
		}

		return (
			<View style={styles.container}>
				<Text style={styles.textDimmingLevel}>
					{`Dimming level: ${this.state.temporaryDimmerValue}%`}
				</Text>
				{slider}
				{toggleButton}
				{learnButton}
			</View>
		);
	}

}

DimmerDeviceDetailModal.propTypes = {
	device: React.PropTypes.object.isRequired,
};

const styles = StyleSheet.create({
	container: {
		flex: 0,
	},
	textDimmingLevel: {
		color: '#1a355b',
		fontSize: 14,
		marginTop: 12,
		marginLeft: 8,
	},
	toggleContainer: {
		flexDirection: 'row',
		height: 36,
		marginHorizontal: 8,
		marginVertical: 16,
	},
	turnOff: {
		flex: 1,
		alignItems: 'stretch',
		borderTopLeftRadius: 7,
		borderBottomLeftRadius: 7,
	},
	turnOn: {
		flex: 1,
		alignItems: 'stretch',
		borderTopRightRadius: 7,
		borderBottomRightRadius: 7,
	},
	learn: {
		height: 36,
		marginHorizontal: 8,
		marginVertical: 8,
		justifyContent: 'center',
		alignItems: 'center',
	},
});

function mapDispatchToProps(dispatch) {
	return {
		onDimmerSlide: (id, value) => dispatch(setDimmerValue(id, value)),
		onDim: (id, value) => dispatch(updateDimmerValue(id, value)),
	};
}

module.exports = connect(null, mapDispatchToProps)(DimmerDeviceDetailModal);
