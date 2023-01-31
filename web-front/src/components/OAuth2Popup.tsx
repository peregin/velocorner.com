// workaround to support HashRouter
// copied from https://github.com/tasoskakour/react-use-oauth2/blob/master/src/components/OAuthPopup.tsx

import React, { useEffect } from 'react';

const OAUTH_STATE_KEY = 'react-use-oauth2-state-key';
const OAUTH_RESPONSE = 'react-use-oauth2-response';

const queryToObject = (query: string) => {
	const parameters = new URLSearchParams(query);
	return Object.fromEntries(parameters.entries());
};

const checkState = (receivedState: string) => {
	const state = sessionStorage.getItem(OAUTH_STATE_KEY);
	return state === receivedState;
};

type Props = {
	OAuthComponent?: React.ReactElement;
};

const OAuth2Popup = (props: Props) => {
	const {
		OAuthComponent = (
			<div style={{ margin: '12px' }} data-testid='popup-loading'>
				Loading...
			</div>
		),
	} = props;

	// On mount
	useEffect(() => {
		const payload = {
			...queryToObject(window.location.href.split('?')[1]),
			...queryToObject(window.location.hash.split('#')[1]),
		};
		const state = payload?.state;
		const error = payload?.error;
		//alert(`state=${state}\n\nstate2=${JSON.stringify(queryToObject(window.location.href.split('?')[1])['state'])}`);
		//debugger

		if (!window.opener) {
			throw new Error('No window opener');
		}

		if (error) {
			window.opener.postMessage({
				type: OAUTH_RESPONSE,
				error: decodeURI(error) || 'OAuth error: An error has occurred.',
			});
		} else if (state && checkState(state)) {
			window.opener.postMessage({
				type: OAUTH_RESPONSE,
				payload,
			});
		} else {
			window.opener.postMessage({
				type: OAUTH_RESPONSE,
				error: `OAuth error: State mismatch.`,
			});
		}
	}, []);

	return OAuthComponent;
};

export default OAuth2Popup;