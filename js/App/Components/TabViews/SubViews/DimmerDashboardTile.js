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

import { View } from 'BaseComponents';
import { Animated, StyleSheet } from 'react-native';
import DashboardShadowTile from './DashboardShadowTile';
import { showDimmerPopup, hideDimmerPopup, setDimmerValue, updateDimmerValue } from 'Actions_Dimmer';
import { turnOn, turnOff, requestTurnOn, requestTurnOff } from 'Actions_Devices';
import VerticalSlider from './VerticalSlider';
import DimmerOffButton from './DimmerOffButton';
import DimmerOnButton from './DimmerOnButton';
import throttle from 'lodash/throttle';

function getDimmerValue(value, isInState) {
	let newValue = value || 0;
	if (isInState === 'TURNON') {
		return 255;
	}
	if (isInState === 'TURNOFF') {
		return 0;
	}

	newValue = parseInt(newValue, 10);
	return newValue;
}

function toDimmerValue(sliderValue) {
	return Math.round(sliderValue * 255 / 100.0);
}

function toSliderValue(dimmerValue) {
	return Math.round(dimmerValue * 100.0 / 255);
}

type Props = {
	item: Object,
	tileWidth: number,
	onDimmerSlide: number => void,
	showDimmerPopup: (name:string, sliderValue:number) => void,
	hideDimmerPopup: () => void,
	onDim: number => void,
	onTurnOn: number => void,
	onTurnOff: number => void,
	onDim: number => void,
	requestTurnOn: number => void,
	requestTurnOff: number => void,
	setScrollEnabled: boolean,
	style: Object,
};

type State = {
	bodyWidth: number,
	bodyHeight: number,
	value: number,
	offButtonFadeAnim: Object,
	onButtonFadeAnim: Object,
};

class DimmerDashboardTile extends View {
	props: Props;
	state: State;
	parentScrollEnabled: boolean;
	onValueChangeThrottled: number => void;
	onTurnOffButtonStart: () => void;
	onTurnOffButtonEnd: () => void;
	onTurnOnButtonStart: () => void;
	onTurnOnButtonEnd: () => void;
	onTurnOn: () => void;
	onTurnOff: () => void;
	layoutView: Object => void;
	onSlidingStart: (name:string, sliderValue:number) => void;
	onSlidingComplete: number => void;
	onValueChange: number => void;

	constructor(props: Props) {
		super(props);
		const { item, onDimmerSlide } = this.props;
		const { value, isInState } = item;
		this.parentScrollEnabled = true;
		this.state = {
			bodyWidth: 0,
			bodyHeight: 0,
			value: getDimmerValue(value, isInState),
			offButtonFadeAnim: new Animated.Value(1),
			onButtonFadeAnim: new Animated.Value(1),
		};

		this.onValueChangeThrottled = throttle(onDimmerSlide(item.id), 200, {
			trailing: true,
		});

		this.onTurnOffButtonStart = this.onTurnOffButtonStart.bind(this);
		this.onTurnOffButtonEnd = this.onTurnOffButtonEnd.bind(this);
		this.onTurnOnButtonStart = this.onTurnOnButtonStart.bind(this);
		this.onTurnOnButtonEnd = this.onTurnOnButtonEnd.bind(this);
		this.onTurnOn = this.onTurnOn.bind(this);
		this.onTurnOff = this.onTurnOff.bind(this);
		this.layoutView = this.layoutView.bind(this);
		this.onSlidingStart = this.onSlidingStart.bind(this);
		this.onSlidingComplete = this.onSlidingComplete.bind(this);
		this.onValueChange = this.onValueChange.bind(this);
	}

	componentWillReceiveProps(nextProps) {
		const { value, isInState } = nextProps.item;

		const dimmerValue = getDimmerValue(value, isInState);
		this.setState({ value: dimmerValue });
	}

	layoutView(x) {
		let { width, height } = x.nativeEvent.layout;
		this.setState({
			bodyWidth: width,
			bodyHeight: height,
		});
	}

	onValueChange(sliderValue) {
		this.onValueChangeThrottled(toDimmerValue(sliderValue));
	}

	onSlidingStart(name:string, sliderValue:number) {
		this.props.showDimmerPopup(name, toDimmerValue(sliderValue));
	}

	onSlidingComplete(sliderValue:number) {
		this.props.onDim(this.props.item.id, toDimmerValue(sliderValue));
		this.props.hideDimmerPopup();
	}

	onTurnOffButtonStart() {
		this.refs.offButton.fadeOut();
	}

	onTurnOffButtonEnd() {
		this.refs.offButton.fadeIn();
	}

	onTurnOnButtonStart() {
		this.refs.onButton.fadeOut();
	}

	onTurnOnButtonEnd() {
		this.refs.onButton.fadeIn();
	}

	onTurnOn() {
		this.props.onTurnOn(this.props.item.id, this.props.item.isInState);
		this.props.requestTurnOn(this.props.item.id);
	}

	onTurnOff() {
		this.props.onTurnOff(this.props.item.id, this.props.item.isInState);
		this.props.requestTurnOff(this.props.item.id);
	}

	render() {
		const { item, tileWidth } = this.props;
		const { name, isInState, supportedMethods, methodRequested } = item;
		const { TURNON, TURNOFF, DIM } = supportedMethods;

		const onButton = <DimmerOnButton ref={'onButton'} isInState={isInState} enabled={!!TURNON} style={styles.turnOn} fontSize={Math.floor(tileWidth / 8)} methodRequested={methodRequested} />;
		const offButton = <DimmerOffButton ref={'offButton'} isInState={isInState} enabled={!!TURNOFF} style={styles.turnOff} fontSize={Math.floor(tileWidth / 8)} methodRequested={methodRequested} />;
		const slider = DIM ?
			<VerticalSlider
				style={[styles.slider, { width: this.state.bodyWidth, height: this.state.bodyHeight, left: 0, bottom: 0 }]}
				thumbWidth={this.state.bodyWidth / 5}
				item={item}
				value={toSliderValue(this.state.value)}
				setScrollEnabled={this.props.setScrollEnabled}
				onSlidingStart={this.onSlidingStart}
				onSlidingComplete={this.onSlidingComplete}
				onValueChange={this.onValueChange}
				onLeftStart={this.onTurnOffButtonStart}
				onLeftEnd={this.onTurnOffButtonEnd}
				onRightStart={this.onTurnOnButtonStart}
				onRightEnd={this.onTurnOnButtonEnd}
				onLeft={this.onTurnOff}
				onRight={this.onTurnOn}
			/> :
			null;
		return (
			<DashboardShadowTile
				isEnabled={isInState === 'TURNON' || isInState === 'DIM'}
				name={name}
				type={'device'}
				tileWidth={tileWidth}
				style={[this.props.style, { width: tileWidth, height: tileWidth }]}>
				<View style={styles.body} onLayout={this.layoutView}>
					{ offButton }
					{ onButton }
					{ slider }
				</View>
			</DashboardShadowTile>
		);
	}
}

const styles = StyleSheet.create({
	container: {
		flex: 1,
		justifyContent: 'center',
	},
	body: {
		flex: 30,
		flexDirection: 'row',
	},
	slider: {
		flex: 1,
		position: 'absolute',
	},
	turnOff: {
		flex: 1,
		alignItems: 'stretch',
		justifyContent: 'center',
		borderTopLeftRadius: 7,
	},
	turnOn: {
		flex: 1,
		alignItems: 'stretch',
		justifyContent: 'center',
		borderTopRightRadius: 7,
	},
});

function mapDispatchToProps(dispatch) {
	return {
		showDimmerPopup: (name:string, value:number) => {
			dispatch(showDimmerPopup(name, value));
		},
		hideDimmerPopup: () => {
			dispatch(hideDimmerPopup());
		},
		onDimmerSlide: id => value => dispatch(setDimmerValue(id, value)),
		onDim: (id, value) => dispatch(updateDimmerValue(id, value)),
		onTurnOn: (id, isInState) => dispatch(turnOn(id, isInState)),
		onTurnOff: (id, isInState) => dispatch(turnOff(id, isInState)),
		requestTurnOn: id => dispatch(requestTurnOn(id)),
		requestTurnOff: id => dispatch(requestTurnOff(id)),
	};
}

module.exports = connect(null, mapDispatchToProps)(DimmerDashboardTile);
